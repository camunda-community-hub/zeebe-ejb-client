package org.camunda.consulting.workers;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.camunda.consulting.services.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camunda.consulting.zeebe_ejb.JobWorker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

@ApplicationScoped
@JobWorker(type = "creditDeduction", autoComplete = false)
public class CustomerCreditWorker implements JobHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(CustomerCreditWorker.class);

  private CustomerService customerService;

  @Inject
  public CustomerCreditWorker(CustomerService customerService) {
    super();
    this.customerService = customerService;
  }

  public CustomerCreditWorker() {
    
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    LOG.info("handler invoked for job {}", job);

    Map<String, Object> resultVariables = creditDeduction(job);

    client.newCompleteCommand(job).variables(resultVariables).send().join();
    LOG.info("handler completed job {}", job);
  }

  private Map<String, Object> creditDeduction(ActivatedJob job) {
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
