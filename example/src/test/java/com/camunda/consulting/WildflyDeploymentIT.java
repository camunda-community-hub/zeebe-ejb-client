package com.camunda.consulting;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

public class WildflyDeploymentIT {

  
  private static final Logger LOG = LoggerFactory.getLogger(WildflyDeploymentIT.class);
  
  private DockerImageName wildflyImage = DockerImageName.parse("quay.io/wildfly/wildfly:26.1.2.Final-jdk11");
  
  @Test
  public void deployToWildfly() {
    LOG.info("Deploying to Wildfly");
    
    Path warToDeployPath = Paths.get("target", "zeebe-ejb-example.war");
    File warToDeploy = warToDeployPath.toFile();
    assertThat(warToDeploy.exists()).isTrue();
    
    MountableFile mountableFile = MountableFile.forHostPath(warToDeployPath);
    String deploymentDir = "/opt/jboss/wildfly/standalone/deployments/";

    GenericContainer<?> wildflyContainer = new GenericContainer<>(wildflyImage);
    wildflyContainer.withExposedPorts(8080, 9990);
    wildflyContainer.withCopyFileToContainer(mountableFile, deploymentDir);
    
    wildflyContainer.start();
    wildflyContainer.followOutput(new Slf4jLogConsumer(LOG));
    
    String host = wildflyContainer.getHost();
    Integer mappedPort = wildflyContainer.getMappedPort(8080);
    LOG.info("Container host: {}", host);
    LOG.info("Container port: {}", mappedPort);
    
    LOG.info("Check web response");
    Client httpClient = ClientBuilder.newClient();
    assertThat(httpClient.target("http://"+host+":"+mappedPort+"/").request().buildGet().invoke().getStatus()).isEqualTo(200);
  }

}
