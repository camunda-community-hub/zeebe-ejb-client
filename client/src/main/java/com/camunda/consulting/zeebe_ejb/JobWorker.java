package com.camunda.consulting.zeebe_ejb;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Inherited
public @interface JobWorker {
  
  String type();

  /**
   * in seconds
   * @return
   */
  long timeout() default 10L;

  /**
   * in seconds
   * @return
   */
  long requestTimeout() default 30L;

  boolean autoComplete() default true;

}
