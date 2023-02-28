package com.camunda.consulting.zeebe_ejb.worker;

import com.camunda.consulting.zeebe_ejb.JobWorker;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import org.camunda.consulting.services.CreditCardService;
import org.camunda.consulting.services.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class Workers {
  private final CustomerService customerService;

  private static final Logger LOG = LoggerFactory.getLogger(Workers.class);
  private final CreditCardService creditCardService;

  @Inject
  public Workers(CustomerService customerService, CreditCardService creditCardService) {
    this.customerService = customerService;
    this.creditCardService = creditCardService;
  }

  @JobWorker(type = "creditCardCharging")
  public void handleChargeCreditCard(JobClient jobClient,  ActivatedJob job) {
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
      jobClient.newThrowErrorCommand(job)
          .errorCode("chargingError")
          .errorMessage("We failed to charge credit card with card number " + cardNumber)
          .send()
          .join();
    }
  }

  @JobWorker(type = "creditDeduction", timeout = 15, autoComplete = false)
  public void handleDeductCredit(JobClient client, ActivatedJob job) throws Exception {
    LOG.info("handler invoked for job {}", job);

    Map<String, Object> resultVariables = creditDeduction(job);

    client.newCompleteCommand(job).variables(resultVariables).send().join();
    LOG.info("handler completed job {}", job);
  }

  public Map<String, Object> creditDeduction(ActivatedJob job) {
    Map<String, Object> variables = job.getVariablesAsMap();
    String customerId = (String) variables.get("customerId");
    Double amount = (Double) variables.get("orderTotal");

    // execute business logic using the variables
    Double openAmount = customerService.deductCredit(customerId, amount);
    Double customerCredit = customerService.getCustomerCredit(customerId);

    // return the results
    Map<String, Object> resultVariables = new HashMap<>();
    resultVariables.put("openAmount", openAmount);
    resultVariables.put("customerCredit", customerCredit);
    return resultVariables;
  }
}
