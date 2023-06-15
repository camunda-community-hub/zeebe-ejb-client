package com.camunda.consulting.zeebe_ejb.rest;

import com.camunda.consulting.zeebe_ejb.ZeebeEjbProcessApplication;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/command")
public class ProcessCommandResource {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessCommandResource.class);

  @POST
  @Path("/start/")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response startPaymentProcessInstance(PaymentPayload payload) {

    LOG.info("payload received: {}", payload);

    final ProcessInstanceEvent processInstanceEvent = startProcessInstance(payload);

    LOG.info("Process instance started: {}", processInstanceEvent.getProcessInstanceKey());
    return Response.status(200).entity(processInstanceEvent).build();
  }

  ProcessInstanceEvent startProcessInstance(PaymentPayload paymentRequest) {
    final ProcessInstanceEvent processInstanceEvent =
        ZeebeEjbProcessApplication.zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("EjbPaymentDelegateExpressionProcess")
            .latestVersion()
            .variables(paymentRequest)
            .send()
            .join();
    return processInstanceEvent;
  }
}
