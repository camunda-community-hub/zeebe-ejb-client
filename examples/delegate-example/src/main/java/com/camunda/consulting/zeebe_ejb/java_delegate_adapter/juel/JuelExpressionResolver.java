package com.camunda.consulting.zeebe_ejb.java_delegate_adapter.juel;

import javax.enterprise.context.ApplicationScoped;
import org.camunda.bpm.engine.cdi.CdiExpressionManager;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.el.JuelExpressionManager;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ExpressionFactory;
import org.camunda.bpm.engine.impl.javax.el.ValueExpression;
import org.camunda.bpm.engine.impl.juel.ExpressionFactoryImpl;
import org.camunda.bpm.engine.impl.juel.SimpleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JuelExpressionResolver {
  
  private static final Logger LOG = LoggerFactory.getLogger(JuelExpressionResolver.class);

  private final JuelExpressionManager expressionManager;
  private final ExpressionFactory expressionFactory;
  private final ELContext elContext;

  public JuelExpressionResolver() {
    this(null, null, null);
    LOG.info("Create blank JuelExpressionResolver");
  }

//  @Inject
  /**
   * 
   * @param expressionManager
   * @param expressionFactory
   * @param elContext
   */
  public JuelExpressionResolver(
      JuelExpressionManager expressionManager,
      ExpressionFactory expressionFactory,
      ELContext elContext) {
    LOG.info("initialize JuelExpressionResolver with {}, {}, {}", expressionManager, expressionFactory, elContext);
//    this.expressionManager = expressionManager;
    this.expressionManager = new CdiExpressionManager();
//    this.elContext = elContext;
    this.elContext = new SimpleContext();
//    this.expressionFactory = expressionFactory;      
    this.expressionFactory = new ExpressionFactoryImpl();      
    LOG.info("initialized JuelExpressionResolver with {}, {}, {}", this.expressionManager, this.expressionFactory, this.elContext);
  }

  public Object evaluate(
      String expressionString, VariableScope variableScope, DelegateExecution execution) {
    LOG.info("evaluating {} using {} in scope {} with {}", expressionString, expressionFactory, variableScope, execution);
    ValueExpression valueExpression =
        expressionFactory.createValueExpression(elContext, expressionString, Object.class);
    return new EnginelessJuelExpression(valueExpression, expressionManager, expressionString)
        .getValue(variableScope, execution);
  }
}
