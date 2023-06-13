package org.camunda.consulting.workers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.camunda.consulting.services.CreditCardService;

import com.camunda.consulting.zeebe_ejb.JobWorker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

@ApplicationScoped
@JobWorker(type = "creditCardCharging" /*, autoComplete = true */)
public class CreditCardWorker implements JobHandler {
  
  private CreditCardService creditCardService;
  
  @Inject
  public CreditCardWorker(CreditCardService creditCardService) {
    super();
    this.creditCardService = creditCardService;
  }  

  public CreditCardWorker() {
    
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    // extract variables from process instance
    String cardNumber = (String) job.getVariablesAsMap().get("cardNumber");
    String cvc = (String) job.getVariablesAsMap().get("cvc");
    String expiryData = (String) job.getVariablesAsMap().get("expiryDate");
    Double amount = (Double) job.getVariablesAsMap().get("openAmount");

    // execute business logic using the variables
    if (cvc.equals("789")) {
      throw new RuntimeException("CVC invalid!");
    }

    try {
      creditCardService.chargeAmount(cardNumber, cvc, expiryData, amount);
    } catch (Exception exc) {
      client
          .newThrowErrorCommand(job)
          .errorCode("chargingError")
          .errorMessage("We failed to charge credit card with card number " + cardNumber)
          .send()
          .join();
    }
  }

}
