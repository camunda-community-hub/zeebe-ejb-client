package com.camunda.consulting.zeebe_ejb.worker;

import com.camunda.consulting.zeebe_ejb.JobWorker;
import org.camunda.consulting.services.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@JobWorker(taskType = "creditDeduction", timeout = 15)
@ApplicationScoped
public class DeductCreditHandler implements JobHandler {

  private final CustomerService service;
  
  private static final Logger LOG = LoggerFactory.getLogger(DeductCreditHandler.class);

  @Inject
  public DeductCreditHandler(CustomerService service) {
    this.service = service;
  }
  
  public DeductCreditHandler() {
    this(null);
  }
  
  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    
    LOG.info("handler invoked for job {}", job);
    
    // extract variables from process instance
    Map<String, Object> variables = job.getVariablesAsMap();
    String customerId = (String) variables.get("customerId");
    Double amount = (Double) variables.get("orderTotal");
    // execute business logic using the variables
    Double openAmount = service.deductCredit(customerId, amount);
    Double customerCredit = service.getCustomerCredit(customerId);
    // save the results to the process instance
    Map<String, Object> resultVariables = new HashMap<>();
    resultVariables.put("openAmount", openAmount);
    resultVariables.put("customerCredit", customerCredit);
    client.newCompleteCommand(job).variables(resultVariables).send().join();
    
    LOG.info("handler completed job {}", job);
  }

}