package com.camunda.consulting.payment_with_java_class;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.camunda.consulting.payment.ProcessConstants;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessUnitTest {

  @Test
  @Disabled("not finished, needs mocking")
  @Deployment(resources = "process.bpmn")
  public void testHappyPath() {
    // Drive the process by API and assert correct behavior by camunda-bpm-assert

    ProcessInstance processInstance = processEngine().getRuntimeService()
        .startProcessInstanceByKey(ProcessConstants.PROCESS_DEFINITION_KEY);

    assertThat(processInstance).isEnded();
  }

}
