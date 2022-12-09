package com.camunda.consulting.zeebe_ejb;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camunda.consulting.zeebe_ejb.worker.ChargeCreditCardHandler;
import com.camunda.consulting.zeebe_ejb.worker.DeductCreditHandler;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Topology;

@Singleton
@Startup
public class ZeebeEjbProcessApplication {
  
  private static final Logger LOG = LoggerFactory.getLogger(ZeebeEjbProcessApplication.class);
  
  public static ZeebeClient zeebeClient;
  
  @Inject
  DeductCreditHandler deductCreditHandler;
  
  @Inject
  ChargeCreditCardHandler chargeCreditCardHandler;
  
  @PostConstruct
  public void start() {
    startWorker();
  }

  private void startWorker() {
    LOG.info("cluster available?");
    zeebeClient = ZeebeClient.newClientBuilder().gatewayAddress("localhost:26500").usePlaintext().build();
    Topology topology = zeebeClient.newTopologyRequest().send().join();
    LOG.info("Cluster Topology: {}", topology);
    
    // Deploy the process model
    DeploymentEvent deploymentEvent = zeebeClient.newDeployResourceCommand().addResourceFromClasspath("ejb-payment-process.bpmn").send().join();
    
    LOG.info("Deployed processes: {}", deploymentEvent.getProcesses());
    
    zeebeClient
        .newWorker()
        .jobType("creditDeduction")
        .handler(deductCreditHandler)
        .timeout(Duration.of(10, ChronoUnit.SECONDS))
        .requestTimeout(Duration.of(30, ChronoUnit.SECONDS))
        .open();
    zeebeClient
        .newWorker()
        .jobType("creditCardCharging")
        .handler(chargeCreditCardHandler)
        .timeout(Duration.of(10, ChronoUnit.SECONDS))
        .requestTimeout(Duration.of(30, ChronoUnit.SECONDS))
        .open();
    
    LOG.info("worker is open");
  }

  @PreDestroy
  public void stop() {
    stopWorker();
  }

  private void stopWorker() {
    LOG.info("close the worker");
    zeebeClient.close();
    LOG.info("worker closed");
  }
  
}
