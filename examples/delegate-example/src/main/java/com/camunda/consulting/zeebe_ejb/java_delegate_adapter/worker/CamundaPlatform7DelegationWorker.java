package com.camunda.consulting.zeebe_ejb.java_delegate_adapter.worker;

import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.camunda.bpm.engine.cdi.CdiArtifactFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camunda.consulting.zeebe_ejb.JobWorker;
import com.camunda.consulting.zeebe_ejb.java_delegate_adapter.execution.ZeebeJobDelegateExecution;
import com.camunda.consulting.zeebe_ejb.java_delegate_adapter.juel.JuelExpressionResolver;

@ApplicationScoped
@JobWorker(type = "camunda-7-adapter", autoComplete = false)
public class CamundaPlatform7DelegationWorker implements JobHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(CamundaPlatform7DelegationWorker.class);

  private final JuelExpressionResolver expressionResolver;
  private final CdiArtifactFactory artifactFactory;

  public CamundaPlatform7DelegationWorker() {
    LOG.info("create delegation worker blank");
    this.expressionResolver = null;
    this.artifactFactory = null;
  }

  @Inject
  public CamundaPlatform7DelegationWorker(
      JuelExpressionResolver expressionResolver, CdiArtifactFactory artifactFactory) {
    LOG.info("Create delegation worker with parameters {}, {}", expressionResolver, artifactFactory);
    this.expressionResolver = expressionResolver;
    this.artifactFactory = artifactFactory;
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    // Read config
    String delegateClass = job.getCustomHeaders().get("class");
    String delegateExpression = job.getCustomHeaders().get("delegateExpression");
    String expression = job.getCustomHeaders().get("expression");
    String resultVariable = job.getCustomHeaders().get("resultVariable");

    // and delegate depending on exact way of implementation
    Map<String, Object> resultPayload = null;
    DelegateExecution execution = wrapDelegateExecution(job);
    // this is required as we add the execution to the variables scope for expression evaluation
    VariableScope variableScope = wrapDelegateExecution(job);
    try {
      if (delegateClass != null) {
        JavaDelegate javaDelegate = loadJavaDelegate(delegateClass);
        javaDelegate.execute(execution);
        resultPayload = execution.getVariables();
      } else if (delegateExpression != null) {
        JavaDelegate javaDelegate =
            (JavaDelegate)
                expressionResolver.evaluate(delegateExpression, variableScope, execution);
        javaDelegate.execute(execution);
        resultPayload = execution.getVariables();
      } else if (expression != null) {
        Object result = expressionResolver.evaluate(expression, variableScope, execution);
        if (resultVariable != null) {
          resultPayload = new HashMap<>();
          resultPayload.put(resultVariable, result);
        }
      } else {
        throw new RuntimeException(
            "Either 'class' or 'delegateExpression' or 'expression' must be specified in task headers for job :"
                + job);
      }

      CompleteJobCommandStep1 completeCommand = client.newCompleteCommand(job.getKey());
      if (resultPayload != null) {
        completeCommand.variables(resultPayload);
      }
      completeCommand.send().join();
    } catch (BpmnError e) {
      throw new UnsupportedOperationException("BPMN Error not yet supported: " + e.getMessage());
//      throw new ZeebeBpmnError(e.getErrorCode(), e.getMessage() == null ? "" : e.getMessage());
    }
  }

  private DelegateExecution wrapDelegateExecution(ActivatedJob job) {
    return new ZeebeJobDelegateExecution(job);
  }

  private JavaDelegate loadJavaDelegate(String delegateName) {
    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Class<? extends JavaDelegate> clazz =
          (Class<? extends JavaDelegate>) contextClassLoader.loadClass(delegateName);
      return artifactFactory.getArtifact(clazz);
    } catch (Exception e) {
      throw new RuntimeException(
          "Could not load delegation class '" + delegateName + "': " + e.getMessage(), e);
    }
  }
}
