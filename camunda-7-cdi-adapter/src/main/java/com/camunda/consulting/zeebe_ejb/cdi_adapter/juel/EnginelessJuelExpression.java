package com.camunda.consulting.zeebe_ejb.cdi_adapter.juel;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.el.JuelExpression;
import org.camunda.bpm.engine.impl.el.JuelExpressionManager;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELException;
import org.camunda.bpm.engine.impl.javax.el.MethodNotFoundException;
import org.camunda.bpm.engine.impl.javax.el.PropertyNotFoundException;
import org.camunda.bpm.engine.impl.javax.el.ValueExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnginelessJuelExpression extends JuelExpression {
  
  private static final Logger LOG = LoggerFactory.getLogger(EnginelessJuelExpression.class);

  public EnginelessJuelExpression(
      ValueExpression valueExpression,
      JuelExpressionManager expressionManager,
      String expressionText) {
    super(valueExpression, expressionManager, expressionText);
    LOG.info("EnginelessJuelExpression initated with {}, {}, {}", valueExpression, expressionManager, expressionText);
  }

  @Override
  public Object getValue(VariableScope variableScope, BaseDelegateExecution contextExecution) {
    LOG.info("get Value with {}, {}", variableScope, contextExecution);
    variableScope.setVariable("execution", contextExecution);
    ELContext elContext = expressionManager.getElContext(variableScope);
    try {
      return valueExpression.getValue(elContext);
    } catch (PropertyNotFoundException pnfe) {
      throw new ProcessEngineException(
          "Unknown property used in expression: "
              + expressionText
              + ". Cause: "
              + pnfe.getMessage(),
          pnfe);
    } catch (MethodNotFoundException mnfe) {
      throw new ProcessEngineException(
          "Unknown method used in expression: " + expressionText + ". Cause: " + mnfe.getMessage(),
          mnfe);
    } catch (ELException ele) {
      throw new ProcessEngineException(
          "EL Error while evaluating expression: "
              + expressionText
              + ". Cause: "
              + ele.getMessage(),
          ele);
    } catch (Exception e) {
      throw new ProcessEngineException(
          "Error while evaluating expression: " + expressionText + ". Cause: " + e.getMessage(), e);
    } finally {
      variableScope.removeVariable("execution");
    }
  }
}
