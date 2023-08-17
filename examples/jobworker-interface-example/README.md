# Example of EJB Process Application with one Worker per Class

In this example, each worker implements the
`io.camunda.zeebe.client.api.worker.JobHandler` interface.

The worker can be easily detected by the framework.

## Start an instance of the example process

```
zbctl create instance EjbPaymentProcessWorkerInterface --variables "{\"customerId\":\"cust30\", \"orderTotal\":40.00, \"cardNumber\":\"1234 5678\", \"cvc\":\"456\", \"expiryDate\":\"09/24\"}" --insecure
```

## Manual test setup

- Local Zeebe cluster with docker-compose
- Local Wildfly Server 2.0
- Deploy Process Application to Wildfly: `mvn clean wildfly:deploy`
- Deploy Process from Modeler to Zeebe
- Start process instance (see above)
- Check the logs from Wildfly
- Double check the instance in Operate
- Undploy Process Application with `mvn wildfly:undeploy`
