package com.camunda.consulting.zeebe_ejb.worker;

import com.camunda.consulting.zeebe_ejb.JobWorker;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.camunda.consulting.services.CreditCardService;

@JobWorker(taskType = "creditCardCharging")
@ApplicationScoped
public class ChargeCreditCardHandler implements JobHandler {
  private final CreditCardService creditCardService;

  @Inject
  public ChargeCreditCardHandler(CreditCardService creditCardService) {
    this.creditCardService = creditCardService;
  }
  
  public ChargeCreditCardHandler() {
    this(null);
  }

  @Override
  public void handle(JobClient jobClient,  ActivatedJob job) {
    // extract variables from process instance
    String cardNumber = (String) job.getVariablesAsMap().get("cardNumber");
    String cvc = (String) job.getVariablesAsMap().get("cvc");
    String expiryData = (String) job.getVariablesAsMap().get("expiryDate");
    Double amount = (Double) job.getVariablesAsMap().get("openAmount");
    
    // execute business logic using the variables
    if (cvc.equals("789")) {
      jobClient
          .newFailCommand(job)
          .retries(0)
          .errorMessage("CVC invalid!")
          .send()
          .join();
    }
    
    try {
      creditCardService.chargeAmount(cardNumber, cvc, expiryData, amount);
      jobClient
          .newCompleteCommand(job)
          .send()
          .join();
    } catch (Exception exc) {
      jobClient
          .newThrowErrorCommand(job)
          .errorCode("chargingError")
          .errorMessage( "We failed to charge credit card with card number " + cardNumber)
          .send()
          .join();
    }
  }
}