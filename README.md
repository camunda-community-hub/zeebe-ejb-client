# Zeebe EJB Client

[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

## Overview

### Zeebe EJB Client Library

This repository contains a library that enables your JavaEE application as a
Client for the Camunda-Platform-8 process engine.

You can reuse your existing JavaEE code and use `@ApplicationScoped` beans in a
job handler as the implementation for BPMN Service tasks and other elements.

The library contains a single startup bean to activate the workers.
[(ZeebeEjbProcessApplication)](client/src/main/java/com/camunda/consulting/zeebe_ejb/ZeebeEjbProcessApplication.java)

All application scoped beans that are annotated with
`@JobWorker(type = "mytype")` are registered as workers on the Zeebe process
engine.

#### Configuration

The Library uses the Zeebe java client configuration properties.

It reads a file named `zeebeClient.properties` from the class-path.

Connection properties for
[Camunda 8 SaaS Cloud cluster](https://docs.camunda.io/docs/components/console/manage-clusters/manage-api-clients/#create-a-client):

```
zeebe.client.cloud.clusterId=xxx
zeebe.client.cloud.clientId=xxx
zeebe.client.cloud.secret=xxx
zeebe.client.cloud.region=bru-2
```

Connection properties for a local installation with
[Docker compose](https://docs.camunda.io/docs/self-managed/platform-deployment/docker/#docker-compose):

```
zeebe.client.gateway.address=localhost:26500
zeebe.client.security.plaintext=true
```

Other client connection properties are mentioned in this class:
[io.camunda.zeebe.client.ClientProperties](https://github.com/camunda/zeebe/blob/main/clients/java/src/main/java/io/camunda/zeebe/client/ClientProperties.java)

### Camunda 7 CDI Adapter

To reuse existing CDI `DelegateExpressions` in Camunda 8, you can add this
library to your project:

```
	<dependency>
	  <groupId>org.camunda.community.client</groupId>
	  <artifactId>camunda-7-cdi-adapter</artifactId>
	  <version>0.0.2-SNAPSHOT</version>
	</dependency>
```

It will use a task type `camunda-7-adapter` and look for configurations like
`<zeebe:header key="delegateExpression" value="${customerCreditDelegate}" />`.
These elements are created during the migration of the BPMN Diagram from Camunda
7 to Camunda 8 with the
[Diagram Converter](https://github.com/camunda-community-hub/camunda-7-to-8-migration/blob/main/backend-diagram-converter/README.md).

The full XML element of a service task using the Camunda 7 CDI adapter:

```
    <bpmn:serviceTask id="Activity_1vwtc60" name="Deduct customer credit">
      <bpmn:extensionElements>
        <zeebe:taskDefinition type="camunda-7-adapter" />
        <zeebe:taskHeaders>
          <zeebe:header key="delegateExpression" value="${customerCreditDelegate}" />
        </zeebe:taskHeaders>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_04h7vab</bpmn:incoming>
      <bpmn:outgoing>Flow_0tr3idd</bpmn:outgoing>
    </bpmn:serviceTask>
```

### Examples

#### Reuse existing Java EE Beans in your new Worker

This repository also contains an example that shows how to reuse existing EJBs
in your Zeebe client on a Java-EE Server.

The Zeebe EJB client library registers all methods and classes that are
annotated with `@JobWorker(taskType="myType")`.

The Job workers require an `@ApplicationScoped` annotation for this. See
[CustomerCreditWorker.java](examples/jobworker-interface-example/src/main/java/org/camunda/consulting/workers/CustomerCreditWorker.java).

The Service implementation itself gets injected to the worker. They are
annotated with `@Stateless`. See
[CustomerService.java](examples/jobworker-interface-example/src/main/java/org/camunda/consulting/services/CustomerService.java)

To run the examples, you can deploy it to a running Wildfly server with
`mvn clean wildfly:deploy`. During deployment all workers are registered in the
Zeebe process engine and get activated.

[You can find the full example here.](examples/jobworker-interface-example/)

##### Start process instances

The example contains a REST Api to start process instances. Check the content of
the package `com.camunda.consulting.zeebe_ejb.rest`.

To start process instances, use a REST client like curl:

curl for cmd.exe

```
curl --location --request POST "http://localhost:8080/zeebe-ejb-example-worker-interface/payment/command/start/" ^
--header "Content-Type: application/json" ^
--data-raw "{\"customerId\": \"12\", \"orderTotal\": 39.99, \"creditCardNumber\": \"1234123412341234\", \"cvc\": \"789\", \"expiryDate\": \"07/24\"}"
```

#### Reuse the DelegateExpression implementation

Just replace the Camunda 7 engine dependency with the camunda-7-cdi-adapter
dependency in the [pom.xml](examples/delegate-example/pom.xml).

And add the
[connection properties](examples/delegate-example/src/main/resources/zeebeClient.properties)
pointing to the Zeebe cluster to your project.

[You can find the complete example here.](examples/delegate-example/)
