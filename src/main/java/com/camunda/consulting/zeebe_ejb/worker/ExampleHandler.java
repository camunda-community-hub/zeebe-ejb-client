package com.camunda.consulting.zeebe_ejb.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camunda.consulting.zeebe_ejb.JobWorker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

public class ExampleHandler implements JobHandler {
  
  
  private static final Logger LOG = LoggerFactory.getLogger(ExampleHandler.class);


  @JobWorker(taskType = "creditDeduction")
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    LOG.info("Example Handler called");

    client.newCompleteCommand(job).send().join();
  }

}
