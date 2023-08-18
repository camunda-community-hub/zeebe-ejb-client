package com.camunda.consulting.payment;

import javax.inject.Inject;
import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.consulting.services.CreditCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class CreditCardDelegate implements JavaDelegate {
  
  private static final Logger LOG = LoggerFactory.getLogger(CreditCardDelegate.class);
  
  CreditCardService creditCardService;

  @Inject
  public CreditCardDelegate(CreditCardService creditCardService) {
    super();
    this.creditCardService = creditCardService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    LOG.info("Credit card delegate for process instance {}", execution.getProcessInstanceId());
    
    creditCardService.chargeAmount(
        (String) execution.getVariable("cardNumber"), 
        (String) execution.getVariable("cvc"), 
        (String) execution.getVariable("expiryDate"), 
        (Double) execution.getVariable("openAmount"));
  }

}
