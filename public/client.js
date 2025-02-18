document.addEventListener('DOMContentLoaded', function() {

  instanceId = Math.floor(Math.random() * 1000);
}, false);


function discoverReader() {
  return fetch("/discover_readers", { method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ instance_id: instanceId })
  }).then((response) => {

    return response.json();
  });
}

function connectReader() {
  return fetch("/connect_reader", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ reader_id: readerId, instance_id: instanceId }),
  }).then((response) => {
    return response.json();
  });
}

function createPaymentIntent(amount) {
  return fetch("/create_payment_intent", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ amount: amount, instance_id: instanceId }),
  }).then((response) => {
    return response.json();
  });
}

function processPayment() {
  return fetch("/process_payment", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      reader_id: readerId,
      instance_id: instanceId,
      payment_intent_id: paymentIntentId,
    }),
  }).then((response) => {
    return response.json();
  });
}

function createSetupIntent() {
  return fetch("/create_setup_intent", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({  instance_id: instanceId }),
  }).then((response) => {
    return response.json();
  });
}

function processSetup() {
  return fetch("/process_setup", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      reader_id: readerId,
      instance_id: instanceId,
      setup_intent_id: setupIntentId,
    }),
  }).then((response) => {
    return response.json();
  });
}

function disconnectReader() {
  return fetch("/disconnect_reader", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      reader_id: readerId,
      instance_id: instanceId
    }),
  }).then((response) => {
    return response.json();
  });
}

function capture(paymentIntentId) {
  return fetch("/capture_payment_intent", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ payment_intent_id: paymentIntentId, instance_id: instanceId }),
  }).then((response) => {
    return response.json();
  });
}

var readerId;
var paymentIntentId;
var setupIntentId;
var instanceId;

// discoverREadersButton
const createLocationButton = document.getElementById("discover-readers-button");
createLocationButton.addEventListener("click", async (event) => {
  createLocationButton.className = "loading";
  createLocationButton.disabled = true;
  discoverReader().then((readers) => {
    createLocationButton.className = "";
    createLocationButton.disabled = false;
    log("[SDK] Discover Readers", readers);
   // locationId = location["id"];
  });
});

const createReaderButton = document.getElementById("connect-reader-button");
createReaderButton.addEventListener("click", async (event) => {
  createReaderButton.className = "loading";
  createReaderButton.disabled = true;
  readerId = document.getElementById("reader-input").value;
  connectReader().then((reader) => {
    createReaderButton.className = "";
    createReaderButton.disabled = false;
    log("[SDK] Connect Reader", reader);
  });
});

const createPaymentButton = document.getElementById("create-payment-button");
createPaymentButton.addEventListener("click", async (event) => {
  createPaymentButton.className = "loading";
  createPaymentButton.disabled = true;
  amount = document.getElementById("amount-input").value;
  createPaymentIntent(amount).then((paymentIntent) => {
    createPaymentButton.className = "";
    createPaymentButton.disabled = false;
    log("[SDK] CreatePaymentIntent", paymentIntent);
    paymentIntentId = paymentIntent["id"];
  });
});

const processPaymentButton = document.getElementById("process-payment-button");
processPaymentButton.addEventListener("click", async (event) => {
  processPaymentButton.className = "loading";
  processPaymentButton.disabled = true;
  processPayment().then((reader) => {
    processPaymentButton.className = "";
    processPaymentButton.disabled = false;
    log(
      "[SDK] ProcessPaymentIntent",
      reader
    );
  });
});

const createSetupButton = document.getElementById("create-setup-button");
createSetupButton.addEventListener("click", async (event) => {
  createSetupButton.className = "loading";
  createSetupButton.disabled = true;
  createSetupIntent().then((setupIntent) => {
    createSetupButton.className = "";
    createSetupButton.disabled = false;
    log("[SDK] Create SetupIntent", setupIntent);
    setupIntentId = setupIntent["id"];
  });
});

const processSetupButton = document.getElementById("process-setup-button");
processSetupButton.addEventListener("click", async (event) => {
  processSetupButton.className = "loading";
  processSetupButton.disabled = true;
  processSetup().then((reader) => {
    processSetupButton.className = "";
    processSetupButton.disabled = false;
    log(
        "[SDK] collectSetupIntentPaymentMethod & confirmSetupIntent",
        reader
    );
  });
});

//disconnectReaderButton
const simulatePaymentButton = document.getElementById(
  "disconnect-reader-button"
);
simulatePaymentButton.addEventListener("click", async (event) => {
  simulatePaymentButton.className = "loading";
  simulatePaymentButton.disabled = true;
  disconnectReader().then((reader) => {
    simulatePaymentButton.className = "";
    simulatePaymentButton.disabled = false;
    log(
      "[SDK] Clear Terminal State",
      reader
    );
  });
});

// const captureButton = document.getElementById("capture-button");
// captureButton.addEventListener("click", async (event) => {
//   captureButton.className = "loading";
//   captureButton.disabled = true;
//   capture(paymentIntentId).then((paymentIntent) => {
//     captureButton.className = "";
//     captureButton.disabled = false;
//     log(
//       "POST /v1/payment_intents/" + paymentIntentId + "/capture",
//       paymentIntent
//     );
//   });
// });

function log(method, message) {
  var logs = document.getElementById("logs");
  var title = document.createElement("div");
  var log = document.createElement("div");
  title.classList.add("row");
  title.classList.add("log-title");
  title.textContent = method;
  log.classList.add("row");
  log.classList.add("log");
  var hr = document.createElement("hr");
  var pre = document.createElement("pre");
  var code = document.createElement("code");
  code.textContent = formatJson(JSON.stringify(message, undefined, 2));
  pre.append(code);
  log.append(pre);
  logs.prepend(hr);
  logs.prepend(log);
  logs.prepend(title);
}

function stringLengthOfInt(number) {
  return number.toString().length;
}

function padSpaces(lineNumber, fixedWidth) {
  // Always indent by 2 and then maybe more, based on the width of the line
  // number.
  return " ".repeat(2 + fixedWidth - stringLengthOfInt(lineNumber));
}

function formatJson(message) {
  var lines = message.split("\n");
  var json = "";
  var lineNumberFixedWidth = stringLengthOfInt(lines.length);
  for (var i = 1; i <= lines.length; i += 1) {
    line = i + padSpaces(i, lineNumberFixedWidth) + lines[i - 1];
    json = json + line + "\n";
  }
  return json;
}