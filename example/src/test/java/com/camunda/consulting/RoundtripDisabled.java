package com.camunda.consulting;

import static org.assertj.core.api.Assertions.*;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.testcontainer.ZeebeProcessTest;
import io.camunda.zeebe.process.test.filters.RecordStream;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import org.camunda.consulting.services.CreditCardService;
import org.camunda.consulting.services.CustomerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
@ZeebeProcessTest
//@ExtendWith(ArquillianExtension.class)
public class RoundtripDisabled {

  private static final Logger LOG = LoggerFactory.getLogger(RoundtripDisabled.class);

  private ZeebeTestEngine engine;
  private ZeebeClient zeebeClient;
  private RecordStream recordStream;

  private Client httpClient;

  //@ArquillianResource 
  private URL base /*= "http://localhost:8080"*/;
  

   @Container
   public GenericContainer wildfly = 
       new GenericContainer("quay.io/wildfly/wildfly:26.1.2.Final-jdk11")
           .withExposedPorts(8080, 9990)
           .withCopyFileToContainer(
               MountableFile.forHostPath(Paths.get("target", "zeebe-ejb-example.war")), 
               "/opt/jboss/wildfly/standalone/deployments/");

   Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOG);
   
  /**
   * 1. Build the war file from this example -> integration-test
   * 2. Start a Zeebe engine with testcontainer -> @ZeebeProcessTest (testcontainers network?)
   * 3. Start a Wildfly instance -> testcontainers
   * 4. Deploy the war file to the test container -> .withCopyFilesToContainer
   * 5. Check for the deployed process model -> (recordStream)
   * 6. start a process instance (POST /command/start {}) 
   * 7. assert for completed process instance -> (recordStream)
   *
   * <p>Resources: 
   * https://github.com/jboss-dockerfiles/wildfly
   * https://www.wildfly.org/news/2022/08/04/wildfly-maven-docker/
   * https://jasondl.ee/2022/wildfly-arquillian-testcontainers-and-kafka
   * https://rieckpil.de/initialization-strategies-with-testcontainers-for-integration-tests/
   * https://www.cs.hs-rm.de/~knauf/JavaEE6/statelessarquillian/index.html
   * https://www.pischka-it.de/2021/02/01/arquillian-mit-junit-5-und-wildfly-server/
   *
   * @throws MalformedURLException
   */
  @Test
  public void roundtripTest() throws MalformedURLException {
    LOG.info("Roundtrip Test started");
    
    wildfly.followOutput(logConsumer);
    String base = "http://" + wildfly.getHost() + ":" + wildfly.getMappedPort(8080);

    WebTarget processStartTarget =
        this.httpClient.target(base + "/command/start/");
    try (final Response processStartResponse =
        processStartTarget.request().accept(MediaType.APPLICATION_JSON).get()) {
      assertThat(processStartResponse.getStatus()).isEqualTo(200);
      // assertThat(greetingGetResponse.readEntity(GreetingMessage.class).getMessage()).startsWith("Say
      // Hello to JakartaEE");
    }
  }

  @BeforeEach
  public void setup() {
    LOG.info("call BeforeEach");
    this.httpClient = ClientBuilder.newClient();
    // removed the Jackson json provider registry, due to OpenLiberty 21.0.0.1
    // switched to use Resteasy.
  }

  @AfterEach
  public void teardown() {
    LOG.info("call AfterEach");
    if (this.httpClient != null) {
      this.httpClient.close();
    }
  }
}
