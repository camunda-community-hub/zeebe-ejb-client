package com.camunda.consulting.zeebe_ejb;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camunda.consulting.zeebe_ejb.worker.ChargeCreditCardHandler;
import com.camunda.consulting.zeebe_ejb.worker.DeductCreditHandler;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Topology;
import io.camunda.zeebe.client.api.worker.JobHandler;

@Singleton
@Startup
public class ZeebeEjbProcessApplication {
  
  private static final Logger LOG = LoggerFactory.getLogger(ZeebeEjbProcessApplication.class);
  
  public static ZeebeClient zeebeClient;
  
  @Inject
  BeanManager beanManager;
  
  @Inject
  DeductCreditHandler deductCreditHandler;
  
  @Inject
  ChargeCreditCardHandler chargeCreditCardHandler;
  
  @PostConstruct
  public void start() {
//    startWorker();
    registerWorkers();
  }
  
  private void registerWorkers() {
    LOG.info("Register workers");
    beanManager.createInstance().select(JobHandler.class).forEach(handler -> {
      Optional.ofNullable(handler.getClass().getAnnotation(JobWorker.class)).ifPresent(annotation -> {
        createWorker(handler, annotation);
      });
    });
  }

  private void createWorker(JobHandler handler, JobWorker annotation) {
    LOG.info("Register handler {} for type {}", handler, annotation.taskType());
    zeebeClient
        .newWorker()
        .jobType(annotation.taskType())
        .handler(handler)
        .timeout(Duration.ofSeconds(10))
        .requestTimeout(Duration.ofSeconds(30))
        .open();
    LOG.info("Worker is open");
  }

  private void startWorker() {
    LOG.info("cluster available?");
    zeebeClient = ZeebeClient.newClientBuilder().gatewayAddress("localhost:26500").usePlaintext().build();
    Topology topology = zeebeClient.newTopologyRequest().send().join();
    LOG.info("Cluster Topology: {}", topology);
    
    // Deploy the process model
    DeploymentEvent deploymentEvent = zeebeClient.newDeployResourceCommand().addResourceFromClasspath("ejb-payment-process.bpmn").send().join();
    
    LOG.info("Deployed processes: {}", deploymentEvent.getProcesses());
    
//    zeebeClient
//        .newWorker()
//        .jobType("creditDeduction")
//        .handler(deductCreditHandler)
//        .timeout(Duration.of(10, ChronoUnit.SECONDS))
//        .requestTimeout(Duration.of(30, ChronoUnit.SECONDS))
//        .open();
//    zeebeClient
//        .newWorker()
//        .jobType("creditCardCharging")
//        .handler(chargeCreditCardHandler)
//        .timeout(Duration.of(10, ChronoUnit.SECONDS))
//        .requestTimeout(Duration.of(30, ChronoUnit.SECONDS))
//        .open();
    
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
