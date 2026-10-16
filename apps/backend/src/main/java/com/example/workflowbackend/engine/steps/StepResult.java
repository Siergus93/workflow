package com.example.workflowbackend.engine.steps;

/**
 * Outcome of executing a single step.
 *
 * @param success whether the step completed successfully
 * @param output handler-specific output, merged into the shared workflow context on success
 * @param errorMessage failure detail, {@code null} on success
 */
public record StepResult(boolean success, Object output, String errorMessage) {

  public static StepResult ok(Object output) {
    return new StepResult(true, output, null);
  }

  public static StepResult failed(String errorMessage) {
    return new StepResult(false, null, errorMessage);
  }
}
