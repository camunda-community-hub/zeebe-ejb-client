package com.camunda.consulting.zeebe_ejb.delegate;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.consulting.services.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class CustomerCreditDelegate implements JavaDelegate {
  
  private static final Logger LOG = LoggerFactory.getLogger(CustomerCreditDelegate.class);
  
  CustomerService customerService;

  @Inject
  public CustomerCreditDelegate(CustomerService customerService) {
    super();
    this.customerService = customerService;
  }
  
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    LOG.info("Delegate for Customer credit started for process instance {}", execution.getProcessInstanceId());

    Map<String, Object> result = creditDeduction(execution);
    
    execution.setVariables(result);
    LOG.info("Customer credit deducted");
  }

  public Map<String, Object> creditDeduction(DelegateExecution execution) {
    Map<String, Object> variables = execution.getVariables();
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
