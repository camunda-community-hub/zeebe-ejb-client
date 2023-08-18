# Example to Debug and Compare Java EE Bean Lookup in Camunda7

## How does it work?

Start process instances with:

```
curl -L "http://localhost:8080/engine-rest/process-definition/key/payment-with-expression/start" -H "Content-Type: application/json" -d "{\"variables\": {\"customerId\": {\"value\": \"cust30\"}, \"orderTotal\": {\"value\": 20.0}}}"
```

or

```
curl -L "http://localhost:8080/engine-rest/process-definition/key/payment-with-expression/start" -H "Content-Type: application/json" -d "{\"variables\": {\"customerId\": {\"value\": \"cust25\"}, \"orderTotal\": {\"value\": 35.0}, \"cardNumber\": {\"value\":\"1234 5678\"}, \"cvc\": {\"value\": \"876\"}, \"expiryDate\": {\"value\": \"09/24\"}}}"
```

## How to use it?

- Enable Eclipse to run a Wildfly Server in Debug mode
- Import the project in Eclipse
- Setup the project and add to Deployment Assembly
  - camunda-engine-cdi.jar
  - camunda-ejb-client.jar
- Add the project to the server
- Run the server in Debug mode

### Breakpoints

- org.camunda.bpm.engine.cdi.impl.el.CdiResolver.getType()
- org.camunda.bpm.engine.cdi.impl.el.CdiResolver.getValue()
- org.camunda.bpm.engine.cdi.impl.el.CdiResolver.invoke()
- com.camunda.consulting.payment_with_java_class.CustomerCreditDelegate.execute()

### Findings

- CdiResolver
  - ProgrammaticBeanManager
    - lookup() finds `CustomerCreditDelegate`

Call happens in
`org.camunda.bpm.engine.impl.bpmn.delegate.JavaDelegateInvocation.invoke()`.

### Unit Test

You can run the JUnit test
[ProcessTest](src/test/java/com/camunda/consulting/payment_with_java_class/ProcessTest.java)
in your IDE or using:

```bash
mvn clean test
```

### Deployment to an Application Server

You can also build and deploy the process application to an application server.
For an easy start you can download JBoss Wildfly with a pre-installed Camunda
from our
[Download Page](https://downloads.camunda.cloud/release/camunda-bpm/wildfly/).

#### Manually

1. Build the application using:

```bash
mvn clean package
```

2. Copy the \*.war file from the `target` directory to the deployment directory
   of your application server e.g. `wildfly/standalone/deployments`. For a
   faster 1-click (re-)deployment see the alternatives below.

#### Wildfly (using Wildfly Maven Plugin)

1. Build and deploy the process application using:

```bash
mvn clean wildfly:deploy
```

#### JBoss AS7 (using JBoss AS Maven Plugin)

1. Build and deploy the process application using:

```bash
mvn clean jboss-as:deploy
```

### Run and Inspect with Tasklist and Cockpit

Once you deployed the application you can run it using
[Camunda Tasklist](http://docs.camunda.org/latest/guides/user-guide/#tasklist)
and inspect it using
[Camunda Cockpit](http://docs.camunda.org/latest/guides/user-guide/#cockpit).

## Environment Restrictions

Built and tested against Camunda Platform version 7.18.0.

## Known Limitations

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

<!-- Tweet
New @Camunda example: Camunda Platform Process Application - A Process Application for [Camunda Platform](http://docs.camunda.org). https://github.com/camunda-consulting/code/tree/master/snippets/payment-with-java-class
-->
