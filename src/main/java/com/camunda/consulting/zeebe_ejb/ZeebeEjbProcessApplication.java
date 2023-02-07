package com.camunda.consulting.zeebe_ejb;

import java.time.Duration;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.Topology;
import io.camunda.zeebe.client.api.worker.JobHandler;

@Singleton
@Startup
public class ZeebeEjbProcessApplication {
  
  private static final Logger LOG = LoggerFactory.getLogger(ZeebeEjbProcessApplication.class);
  
  public static ZeebeClient zeebeClient;
  
  @Inject
  BeanManager beanManager;
  
  @PostConstruct
  public void start() {
    clusterConnection();
    registerWorkers();
  }
  
  private void registerWorkers() {
    // TODO: support annotation on method
    LOG.info("Register workers");
    beanManager.createInstance()
        //.select(ApplicationScoped.class)
        .select(JobHandler.class).forEach(handler -> {
      Optional.ofNullable(handler.getClass().getAnnotation(JobWorker.class)).ifPresent(annotation -> {
        if (annotation.autoComplete()) {
          createWorker(new AutoCompleteWrapper(handler), annotation);
        } else {
          createWorker(handler, annotation);
        }
      });
    });
  }

  private void createWorker(JobHandler handler, JobWorker annotation) {
    LOG.info("Register handler {} for type {}", handler, annotation.taskType());
    zeebeClient
        .newWorker()
        .jobType(annotation.taskType())
        .handler(handler)
        .timeout(Duration.ofSeconds(annotation.timeout()))
        .requestTimeout(Duration.ofSeconds(annotation.requestTimeout()))
        .open();
    LOG.info("Worker is open");
  }

  private void clusterConnection() {
    LOG.info("cluster available?");
    zeebeClient = ZeebeClient.newClientBuilder().gatewayAddress("localhost:26500").usePlaintext().build();
    Topology topology = zeebeClient.newTopologyRequest().send().join();
    LOG.info("Cluster Topology: {}", topology);
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
