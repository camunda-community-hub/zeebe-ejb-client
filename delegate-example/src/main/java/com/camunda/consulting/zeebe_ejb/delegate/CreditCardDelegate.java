package com.camunda.consulting.zeebe_ejb.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditCardDelegate implements JavaDelegate {

  private static final Logger LOG = LoggerFactory.getLogger(CreditCardDelegate.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    LOG.info("Credit card delegate for process instance {}", execution.getProcessInstanceId());

  }

}
