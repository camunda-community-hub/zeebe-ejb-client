package com.camunda.consulting.zeebe_ejb;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

public class AutoCompleteWrapper implements JobHandler {

  private JobHandler handler;

  public AutoCompleteWrapper(JobHandler handler) {
    this.handler = handler;
  }

  //      if (workerValue.getAutoComplete()) {
  //        CommandWrapper command = new CommandWrapper(
  //            createCompleteCommand(jobClient, job, result),
  //            job,
  //            commandExceptionHandlingStrategy);
  //        command.executeAsync();
  //      }
  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    try {
      // result = workerValue.getMethodInfo().invoke(args.toArray())
      handler.handle(client, job);
      client.newCompleteCommand(job).send().join();
    } catch (Exception exception) {
      client.newFailCommand(job).retries(0).errorMessage(exception.getMessage()).send().join();
    }
  }
}
