package com.example.workflowbackend.engine.steps.impl;

import com.example.workflowbackend.engine.context.WorkflowContext;
import com.example.workflowbackend.engine.steps.StepHandler;
import com.example.workflowbackend.engine.steps.StepResult;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Pauses the run. Config: {@code seconds} (number, required). */
@Component
public class DelayStepHandler implements StepHandler {

  private static final String TYPE = "delay";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public StepResult execute(Map<String, Object> config, WorkflowContext context) {
    Object raw = config.get("seconds");
    if (!(raw instanceof Number number)) {
      return StepResult.failed("delay step requires a numeric 'seconds' config");
    }
    long seconds = number.longValue();
    if (seconds < 0) {
      return StepResult.failed("delay step 'seconds' must not be negative");
    }
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return StepResult.failed("delay step was interrupted");
    }
    return StepResult.ok(Map.of("delayedSeconds", seconds));
  }
}
