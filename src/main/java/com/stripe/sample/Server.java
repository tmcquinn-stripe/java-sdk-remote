package com.stripe.sample;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFiles;
import static spark.Spark.port;

import com.google.gson.Gson;

import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.model.terminal.Reader;
import com.stripe.param.terminal.ReaderProcessPaymentIntentParams;
import com.stripe.exception.InvalidRequestException;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.appinfo.ApplicationInformation;
import com.stripe.stripeterminal.external.callable.InternetReaderListener;
import com.stripe.stripeterminal.external.callable.ReaderCallback;
import com.stripe.stripeterminal.external.callable.ReadersCallback;
import com.stripe.stripeterminal.external.callable.TerminalListener;
import com.stripe.stripeterminal.external.models.ConnectionConfiguration;
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration;
import com.stripe.stripeterminal.external.models.TerminalException;
import com.stripe.stripeterminal.log.LogLevel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class Server {
  private static Gson gson = new Gson();

  static class PaymentIntentParams {
    private String payment_intent_id;
    private long amount;
    private String instance_id;


    public String getPaymentIntentId() {
      return payment_intent_id;
    }

    public String getInstanceId() { return instance_id; }
    public long getAmount() {
      return amount;
    }
  }

  static class ReaderParams {
    private String reader_id;
    private String instance_id;

    // private String location_id;
   public String getInstanceId() { return instance_id; }

    public String getReaderId() {
      return reader_id;
    }

//    public String getLocationId() {
//      return location_id;
//    }
  }

  static class ProcessPaymentParams {
    private String reader_id;
    private String payment_intent_id;
    private String instance_id;

    public String getReaderId() {
      return reader_id;
    }
    public String getInstanceId() { return instance_id; }

    public String getPaymentIntentId() {
      return payment_intent_id;
    }
  }

  static class DiscoverReaderParams {
    private String instance_id;

    public String getInstanceId() { return instance_id; }

  }

  public static void main(String[] args) {
    port(4242);
    staticFiles.externalLocation(Paths.get("public").toAbsolutePath().toString());

    TerminalInterface terminalInterface = new TerminalInterface();

// Since the Terminal is a singleton, you can call getInstance whenever you need it.
    Terminal.getInstance();

    post("/discover_readers", (request, response) -> {
        try {
            DiscoverReaderParams postBody = gson.fromJson(request.body(), DiscoverReaderParams.class);
            CompletableFuture<List<com.stripe.stripeterminal.external.models.Reader>> f = new CompletableFuture<>();

            f = terminalInterface.getCurrentDiscoveryList(postBody.getInstanceId());
            return gson.toJson(f.get());
        } catch (LockedException | ExecutionException e) {
            return gson.toJson(e.getMessage());
        }
    });


    post("/connect_reader", (request, response) -> {
        try {
            ReaderParams postBody = gson.fromJson(request.body(), ReaderParams.class);
            String readerId = postBody.getReaderId();

            CompletableFuture<com.stripe.stripeterminal.external.models.Reader> f = terminalInterface.connectToReader(readerId, postBody.getInstanceId());

            // Need to fetch the list again to make sure everything is still there
            // Add unlock reader retries or something
            if (f.get() == null) {
              return gson.toJson(f.get());
            }

            return gson.toJson(f.get());
        } catch (LockedException | ExecutionException e) {
          return gson.toJson(e.getMessage());
        }
    });

    post("/create_payment_intent", (request, response) -> {
        try {
            response.type("application/json");
            PaymentIntentParams postBody = gson.fromJson(request.body(), PaymentIntentParams.class);

            CompletableFuture<com.stripe.stripeterminal.external.models.PaymentIntent> f = terminalInterface
                    .createPaymentIntent((long)postBody.getAmount(), postBody.getInstanceId());

            return gson.toJson(f.get());
        } catch (LockedException | ExecutionException e) {
          return gson.toJson(e.getMessage());
        }
    });

    post("/create_setup_intent", (request, response) -> {
        try {
            response.type("application/json");
            PaymentIntentParams postBody = gson.fromJson(request.body(), PaymentIntentParams.class);


            CompletableFuture<com.stripe.stripeterminal.external.models.SetupIntent> f = terminalInterface
                    .createSetupIntent(postBody.getInstanceId());


            return gson.toJson(f.get());
        } catch (LockedException | ExecutionException e) {
          return gson.toJson(e.getMessage());
        }
    });


    post ("/get_locked_status", (request, response) -> {
      ProcessPaymentParams postBody = gson.fromJson(request.body(), ProcessPaymentParams.class);

      return gson.toJson(terminalInterface.getInstanceID());
    });

    // This actually returns the PI back, so i could retrieve it, but we will just cache for now...
    post("/process_payment", (request, response) -> {
      ProcessPaymentParams postBody = gson.fromJson(request.body(), ProcessPaymentParams.class);

      int attempt = 0;
      int tries = 3;
      while (true) {
        attempt++;
        try {

          CompletableFuture<com.stripe.stripeterminal.external.models.PaymentIntent> f = terminalInterface
                  .processPayment(postBody.getInstanceId());

          System.out.println("Successful PI - Unlocking resource");

          if (f.get() != null) {
            terminalInterface.clearState();
          }

          return gson.toJson(f.get());
        } catch (ExecutionException e) {
          Throwable cause = e.getCause();

          if (cause instanceof TerminalException) {
            TerminalException te = (TerminalException) cause;
            switch (te.getErrorCode().toString()) {
              case "terminal_reader_timeout":
                // Temporary networking blip, automatically retry a few times.
                if (attempt == tries) {
                  return gson.toJson(te.getErrorMessage());
                }
                break;
              case "terminal_reader_offline":
                // Reader is offline and won't respond to API requests. Make sure the reader is
                // powered on and connected to the internet before retrying.
                return gson.toJson(te.getErrorMessage());
              case "terminal_reader_busy":
                // Reader is currently busy processing another request, installing updates or
                // changing settings. Remember to disable the pay button in your point-of-sale
                // application while waiting for a reader to respond to an API request.
                return gson.toJson(te.getErrorMessage());
              case "intent_invalid_state":
                // Check PaymentIntent status because it's not ready to be processed. It might
                // have been already successfully processed or canceled.
                PaymentIntent paymentIntent = PaymentIntent.retrieve(postBody.getPaymentIntentId());
                Map<String, String> errorResponse = Collections.singletonMap("error",
                        "PaymentIntent is already in " + paymentIntent.getStatus() + " state.");
                return gson.toJson(errorResponse);

              default:
                return gson.toJson(te.getErrorMessage());
            }
          } else {
            e.printStackTrace();

            System.out.println("Something unexpected happened");
            return gson.toJson("Something unexpected happened");
          }
        }
      }
    });

    post("/process_setup", (request, response) -> {
      ProcessPaymentParams postBody = gson.fromJson(request.body(), ProcessPaymentParams.class);

      int attempt = 0;
      int tries = 3;
      while (true) {
        attempt++;
        try {

          CompletableFuture<com.stripe.stripeterminal.external.models.SetupIntent> f = terminalInterface
                  .processSetup(postBody.getInstanceId());

          System.out.println("Successful SI - Unlocking resource");

          if (f.get() != null) {
            terminalInterface.clearState();
          }

          return gson.toJson(f.get());
        } catch (ExecutionException e) {
          Throwable cause = e.getCause();

          if (cause instanceof TerminalException) {
            TerminalException te = (TerminalException) cause;
            switch (te.getErrorCode().toString()) {
              case "terminal_reader_timeout":
                // Temporary networking blip, automatically retry a few times.
                if (attempt == tries) {
                  return gson.toJson(te.getErrorMessage());
                }
                break;
              case "terminal_reader_offline":
                // Reader is offline and won't respond to API requests. Make sure the reader is
                // powered on and connected to the internet before retrying.
                return gson.toJson(te.getErrorMessage());
              case "terminal_reader_busy":
                // Reader is currently busy processing another request, installing updates or
                // changing settings. Remember to disable the pay button in your point-of-sale
                // application while waiting for a reader to respond to an API request.
                return gson.toJson(te.getErrorMessage());
              case "intent_invalid_state":
                // Check PaymentIntent status because it's not ready to be processed. It might
                // have been already successfully processed or canceled.
                PaymentIntent paymentIntent = PaymentIntent.retrieve(postBody.getPaymentIntentId());
                Map<String, String> errorResponse = Collections.singletonMap("error",
                        "SetupIntent is already in " + paymentIntent.getStatus() + " state.");
                return gson.toJson(errorResponse);

              default:
                return gson.toJson(te.getErrorMessage());
            }
          } else {
            e.printStackTrace();

            System.out.println("Something unexpected happened");
            return gson.toJson("Something unexpected happened");
          }
        }
      }
    });

    post("/disconnect_reader", (request, response) -> {
      ReaderParams postBody = gson.fromJson(request.body(), ReaderParams.class);

      terminalInterface.clearState();

      return gson.toJson("Unlocked Terminal");
    });

    post("/capture_payment_intent", (request, response) -> {
      response.type("application/json");

      PaymentIntentParams postBody = gson.fromJson(request.body(), PaymentIntentParams.class);

      PaymentIntent intent = PaymentIntent.retrieve(postBody.getPaymentIntentId());
      intent = intent.capture();

      return intent.toJson();
    });
  }


}