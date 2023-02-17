# Zeebe EJB Client

[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

## Overview

### Library
This repository contains a library that enables your JavaEE application as a Client for the Camunda-Platform-8 process engine.

You can reuse your existing JavaEE code and use `@ApplicationScoped` beans in a job handler as the implementation for BPMN Service tasks and other elements.

The library contains a single startup bean to activate the workers. [(ZeebeEjbProcessApplication)](client/src/main/java/com/camunda/consulting/zeebe_ejb/ZeebeEjbProcessApplication.java)

All application scoped beans that are annotated with `@JobWorker(type = "mytype")` are registered as workers on the Zeebe process engine.
 
### Example
It also contains example shows how to reuse existing EJBs in your Zeebe client on an Java-EE Server.

This Zeebe EJB client library registers all jobworker implementations that are annotated with `@JobWorker(taskType="myType")`.

The Job workers require an `@ApplicationScoped` for this. See [DeductCreditHandler](example/src/main/java/com/camunda/consulting/zeebe_ejb/worker/DeductCreditHandler.java).

The Service implementation itself gets injected to the worker. They are annotated with `@Stateless`. See [CustomerService](example/src/main/java/org/camunda/consulting/services/CustomerService.java) 

To run the examples, you can deploy it to a running Wildfly server with `mvn clean wildfly:deploy`. During deployment all workers are registered in the Zeebe process engine and get activated.

## Start process instances

The example contains a REST Api to start process instances. Check the content of the package `com.camunda.consulting.zeebe_ejb.rest`.

To start process instances, use a REST client like curl:

curl for cmd.exe

```
curl --location --request POST "http://localhost:8080/zeebe-ejb-client/payment/command/start/" ^
--header "Content-Type: application/json" ^
--data-raw "{\"customerId\": \"12\", \"orderTotal\": 39.99, \"creditCardNumber\": \"1234123412341234\", \"cvc\": \"789\", \"expiryDate\": \"07/24\"}"
```