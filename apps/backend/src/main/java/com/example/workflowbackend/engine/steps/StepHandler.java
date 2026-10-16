package com.example.workflowbackend.engine.steps;

import com.example.workflowbackend.engine.context.WorkflowContext;
import java.util.Map;

/**
 * Executes one workflow step type. Implementations are Spring beans discovered by the
 * {@link StepHandlerRegistry}.
 */
public interface StepHandler {

  /** The step {@code type} this handler is responsible for, e.g. {@code "log"} or {@code "delay"}. */
  String getType();

  /**
   * Runs the step.
   *
   * @param config step configuration from the workflow JSON (never {@code null})
   * @param context shared run context; handlers may read and write it
   * @return the outcome, including any output to expose to later steps
   */
  StepResult execute(Map<String, Object> config, WorkflowContext context);
}
