package com.stripe.sample;

import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.appinfo.ApplicationInformation;
import com.stripe.stripeterminal.external.callable.*;
import com.stripe.stripeterminal.external.models.*;
import com.stripe.stripeterminal.log.LogLevel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TerminalInterface {
    public PaymentIntent currentPaymentIntent;

    public SetupIntent getCurrentSetupIntent() {
        return currentSetupIntent;
    }

    public void setCurrentSetupIntent(SetupIntent currentSetupIntent) {
        this.currentSetupIntent = currentSetupIntent;
    }

    public SetupIntent currentSetupIntent;

    public Reader currentReader;

    public boolean isLocked(String instanceID) {
        if (getInstanceID() == null) {
            return false;
        }
        else if (getInstanceID().equals(instanceID)) {
            return false;
        }
        else {
            return true;
        }
    }

    public String getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }

    public String instanceID;


    public PaymentIntent getCurrentPaymentIntent() {
        return currentPaymentIntent;
    }

    public void setCurrentPaymentIntent(PaymentIntent currentPaymentIntent) {
        this.currentPaymentIntent = currentPaymentIntent;
    }

    public Reader getCurrentReader() {
        return currentReader;
    }

    public void setCurrentReader(Reader currentReader) {
        this.currentReader = currentReader;
    }

    public TerminalInterface() {
        TerminalListener listener = new CustomTerminalListener();

// Choose the level of messages that should be logged to your console.
        LogLevel logLevel = LogLevel.VERBOSE;

// Create your token provider.
        CustomConnectionTokenProvider tokenProvider = new CustomConnectionTokenProvider();

// Create your application information & pass in an existing data directory for your app.
        Path path = FileSystems.getDefault().getPath(System.getProperty("user.home"), "myTestApp");
        File appDir = path.toFile();
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        ApplicationInformation appInfo = new ApplicationInformation("myTestApp", "1.2.3", appDir);

// Pass in the listener you created, token provider, application information, your desired logging level, and an optional offline listener.
        if (!Terminal.isInitialized()) {
            Terminal.initTerminal(tokenProvider, listener, appInfo, logLevel, null);
        }
    }

    public CompletableFuture<List<com.stripe.stripeterminal.external.models.Reader>> getCurrentDiscoveryList(String instanceID) {


        CompletableFuture<List<com.stripe.stripeterminal.external.models.Reader>> f = new CompletableFuture<>();

        if (isLocked(instanceID)) {
            System.out.println("Is locked");
            f.completeExceptionally(new LockedException());
            return f;
        } else {
            setInstanceID(instanceID);
        }

        DiscoveryConfiguration.InternetDiscoveryConfiguration config = new DiscoveryConfiguration.InternetDiscoveryConfiguration(
                3, null, false
        );

        Terminal.getInstance().discoverReaders(
                config,
                new ReadersCallback() {
                    @Override
                    public void onSuccess(@org.jetbrains.annotations.NotNull List<com.stripe.stripeterminal.external.models.Reader> list) {
                        f.complete(list);
                    }

                    @Override
                    public void onFailure(@org.jetbrains.annotations.NotNull TerminalException e) {
                        f.completeExceptionally(e);
                    }
                }
        );
        return f;
    }

    //readerId is actually the reader label
    public CompletableFuture<com.stripe.stripeterminal.external.models.Reader> connectToReader(String readerId, String instanceID) throws ExecutionException, InterruptedException {
        CompletableFuture<com.stripe.stripeterminal.external.models.Reader> readerF = new CompletableFuture<>();

        if (isLocked(instanceID)) {
            readerF.completeExceptionally(new LockedException());
            return readerF;
        }

        CompletableFuture<List<com.stripe.stripeterminal.external.models.Reader>> f = getCurrentDiscoveryList(instanceID);
        List<com.stripe.stripeterminal.external.models.Reader> readers = f.get();
        com.stripe.stripeterminal.external.models.Reader selectedReader = null;

        for (com.stripe.stripeterminal.external.models.Reader reader : readers) {
            System.out.println(reader.getLabel());
            System.out.println(readerId);

            assert reader.getLabel() != null;
            if (reader.getLabel().equals(readerId.trim())) {
                selectedReader = reader;
            }
        }

        if (selectedReader == null) {
            System.out.println("NO READER");
            readerF.complete(null);

            return readerF;
        }

        ConnectionConfiguration.InternetConnectionConfiguration config = new ConnectionConfiguration.InternetConnectionConfiguration(false, new CustomInternetReaderListener());

        Terminal.getInstance().connectReader(
                selectedReader,
                config,
                new ReaderCallback() {
                    @Override
                    public void onSuccess(@NotNull com.stripe.stripeterminal.external.models.Reader reader) {
                        System.out.println("success :)");
                        setCurrentReader(reader);
                        readerF.complete(reader);
                    }

                    @Override
                    public void onFailure(@NotNull TerminalException e) {
                        System.out.println("Error occurred connecting to reader");

                        readerF.completeExceptionally(e);
                        e.printStackTrace();
                    }
                }
        );
        return readerF;
    }

    public CompletableFuture<PaymentIntent> createPaymentIntent(long amount, String instanceID) {
        CompletableFuture<PaymentIntent> fPI = new CompletableFuture<>();

        if (isLocked(instanceID)) {
            fPI.completeExceptionally(new LockedException());
            return fPI;
        }

        PaymentIntentParameters params = new PaymentIntentParameters.Builder()
                .setAmount(amount)
                .setCurrency("usd")
                .build();

        Terminal.getInstance().createPaymentIntent(params, null, new PaymentIntentCallback() {
            @Override
            public void onSuccess(@NotNull PaymentIntent paymentIntent) {
                setCurrentPaymentIntent(paymentIntent);
                fPI.complete(paymentIntent);
            }

            @Override
            public void onFailure(@NotNull TerminalException e) {
                System.out.println("Error occurred creating a PI");
                fPI.completeExceptionally(e);
            }
        });

        return fPI;
    }

    public void clearState() {
        setCurrentReader(null);
        setCurrentPaymentIntent(null);
        setInstanceID(null);

        Terminal.getInstance().disconnectReader(new Callback() {
            @Override
            public void onSuccess() {
                System.out.println("disconnected from reader manually w/ clear state");
            }

            @Override
            public void onFailure(@NotNull TerminalException e) {
                e.printStackTrace();
            }
        });
    }

    // An object that contains the PI _and_ the cancellable would be nice so I can timeout
    public CompletableFuture<PaymentIntent> processPayment(String instanceID) {
        CompletableFuture<PaymentIntent> fPI = new CompletableFuture<>();
        if (isLocked(instanceID)) {
            fPI.completeExceptionally(new LockedException());
            return fPI;
        }

        CollectConfiguration collectConfiguration = new CollectConfiguration.Builder()
                .setMoto(false)
                .build();

        Cancelable cancelable = Terminal.getInstance().collectPaymentMethod(
            getCurrentPaymentIntent(), collectConfiguration, new PaymentIntentCallback() {
                @Override
                public void onSuccess(@NotNull PaymentIntent paymentIntent) {
                    // Do confirm in same step...
                    Terminal.getInstance().confirmPaymentIntent(
                            paymentIntent,
                            new PaymentIntentCallback() {
                                @Override
                                public void onSuccess(@NotNull PaymentIntent paymentIntent) {
                                    fPI.complete(paymentIntent);
                                }

                                @Override
                                public void onFailure(@NotNull TerminalException e) {
                                    System.out.println("Something went wrong confirming the PaymentIntent");
                                    fPI.completeExceptionally(e);
                                }
                            }
                    );
                }

                @Override
                public void onFailure(@NotNull TerminalException e) {
                    System.out.println("Something went wrong collecting a PaymentMethod");
                    fPI.completeExceptionally(e);
                }
            }
        );

        return fPI;
    }

    public CompletableFuture<SetupIntent> createSetupIntent(String instanceID) {
        CompletableFuture<SetupIntent> fSI = new CompletableFuture<>();
        if (isLocked(instanceID)) {
            fSI.completeExceptionally(new LockedException());
            return fSI;
        }

        SetupIntentParameters params = new SetupIntentParameters.Builder().build();

        Terminal.getInstance().createSetupIntent(params, new SetupIntentCallback() {
            @Override
            public void onSuccess(@NotNull SetupIntent setupIntent) {
                System.out.println("SetupIntent Creation Completed");
                setCurrentSetupIntent(setupIntent);
                fSI.complete(setupIntent);
            }

            @Override
            public void onFailure(@NotNull TerminalException e) {
                System.err.println("SetupIntent Creation Failed");
                fSI.completeExceptionally(e);
            }
        });
        return fSI;
    }

    public CompletableFuture<SetupIntent> processSetup(String instanceID) {
        CompletableFuture<SetupIntent> fSI = new CompletableFuture<>();
        if (isLocked(instanceID)) {
            fSI.completeExceptionally(new LockedException());
            return fSI;
        }

        Cancelable cancelable = Terminal.getInstance().collectSetupIntentPaymentMethod(getCurrentSetupIntent(), AllowRedisplay.LIMITED , new SetupIntentCallback() {
            @Override
            public void onSuccess(@NotNull SetupIntent setupIntent) {
                System.out.println("SetupIntent Collection Completed");

                // Moving on to process the SetupIntent
                Terminal.getInstance().confirmSetupIntent(setupIntent, new SetupIntentCallback() {
                    @Override
                    public void onSuccess(@NotNull SetupIntent setupIntent) {
                        System.out.println("SetupIntent confirmation succeeded");
                        fSI.complete(setupIntent);
                    }

                    @Override
                    public void onFailure(@NotNull TerminalException e) {
                        System.err.println("SetupIntent processing failed");
                        fSI.completeExceptionally(e);
                    }
                });
            }

            @Override
            public void onFailure(@NotNull TerminalException e) {
                System.err.println("SetupIntent failed at collection");
                fSI.completeExceptionally(e);
            }
        });

        return fSI;
    }
}