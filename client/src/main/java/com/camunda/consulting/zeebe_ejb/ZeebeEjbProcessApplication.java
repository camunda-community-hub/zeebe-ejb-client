package com.camunda.consulting.zeebe_ejb;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJBContext;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.spi.EJBContainerProvider;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.naming.InitialContext;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.ClientProperties;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.Topology;
import io.camunda.zeebe.client.api.worker.JobHandler;

@Singleton
@Remote
@Startup
public class ZeebeEjbProcessApplication {

  public static final String CONFIGURATION_PROPERTIES = "zeebeClient.properties";
  
  private static final Logger LOG = LoggerFactory.getLogger(ZeebeEjbProcessApplication.class);
  
  public static ZeebeClient zeebeClient;
  
  @Inject
  BeanManager beanManager;

  @Inject
  @Any
  Instance<Object> beans;
  
  @PostConstruct
  public void start() {
    Properties config = null;
    try {
      config = loadZeebeClientConfig();
      clusterConnection(config);
    } catch (Exception e) {
      // Abort deployment
      throw new RuntimeException(e.getLocalizedMessage());
    }
    registerWorkers();
  }
  
  private void registerWorkers() {
    LOG.info("Register workers");
    Instance<Object> beans = beanManager.createInstance();
        //.select(ApplicationScoped.class)
    beans
        .select(JobHandler.class).forEach(handler -> {
      Optional.ofNullable(handler.getClass().getAnnotation(JobWorker.class)).ifPresent(annotation ->
          createWorker(handler, annotation));
    });
    beanManager.getBeans(Object.class,new AnnotationLiteral<Any>() {})
        //.select(ApplicationScoped.class)
        .forEach(bean -> {Arrays.stream(bean.getBeanClass().getMethods())
            .forEach(method -> {
              Optional.ofNullable(method.getAnnotation(JobWorker.class)).ifPresent(annotation -> {
                JobHandler handler = (client, job) -> method.invoke(bean, client, job);
                createWorker(handler, annotation);
              });});});
  }

  private void createWorker(JobHandler handler, JobWorker annotation) {
    LOG.info("Register handler {} for type {}", handler, annotation.type());
    zeebeClient
        .newWorker()
        .jobType(annotation.type())
        .handler(annotation.autoComplete() ? new AutoCompleteWrapper(handler) : handler)
        .timeout(Duration.ofSeconds(annotation.timeout()))
        .requestTimeout(Duration.ofSeconds(annotation.requestTimeout()))
        .open();
    LOG.info("Worker is open");
  }
  
  private void clusterConnection(Properties config) {
    LOG.info("Cloud cluster available?");
    ZeebeClientBuilder zeebeClientBuilder;
    if (config.getProperty(ClientProperties.CLOUD_CLUSTER_ID) != null) {
      zeebeClientBuilder = ZeebeClient.newCloudClientBuilder()
          .withClusterId(config.getProperty(ClientProperties.CLOUD_CLUSTER_ID))
          .withClientId(config.getProperty(ClientProperties.CLOUD_CLIENT_ID))
          .withClientSecret(ClientProperties.CLOUD_CLIENT_SECRET);      
    } else {
      zeebeClientBuilder = ZeebeClient.newClientBuilder();
    }
    
    zeebeClient = zeebeClientBuilder.withProperties(config).build();
    LOG.info("Creating ZeebeClient using {}", zeebeClientBuilder);
        
    Topology topology = zeebeClient.newTopologyRequest().send().join();
    LOG.info("Cluster Topology: {}", topology);
  }

  private Properties loadZeebeClientConfig() throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream input = classLoader.getResourceAsStream(CONFIGURATION_PROPERTIES);
    if (input == null) {
      throw new IOException("'" + CONFIGURATION_PROPERTIES + "' not found");
    }
    Properties properties = new Properties();
    properties.load(input);
    return properties;
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
