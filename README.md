# Accept in-person payments

Set up the Stripe Terminal SDK so you can begin accepting in-person payments. Included are some basic build and run scripts you can use to start up the application. This integration is based off of the Terminal SDI example application here: https://docs.stripe.com/terminal/quickstart?client=java&platform=server-driven. SDI calls were replaced in favor of a Terminal Java SDK implementation.

## Running the sample

1. Build the server

~~~
mvn package
~~~

2. Run the server

~~~
java -cp target/sample-jar-with-dependencies.jar com.stripe.sample.Server
~~~


3. Go to [http://localhost:4242](http://localhost:4242)
