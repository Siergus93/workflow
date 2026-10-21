package com.example.workflowbackend.engine.steps.impl;

import com.example.workflowbackend.engine.context.WorkflowContext;
import com.example.workflowbackend.engine.steps.StepHandler;
import com.example.workflowbackend.engine.steps.StepResult;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Writes a value into the shared workflow context for later steps to read.
 *
 * <p>Config: {@code key} (string, required, non-blank) and {@code value} (any JSON value, required;
 * {@code null} allowed).
 *
 * <p>Returns a single-entry map so the engine's context merge stores it under {@code key} and the
 * value is also persisted as this step's output.
 */
@Component
public class SetStepHandler implements StepHandler {

  private static final String TYPE = "set";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public StepResult execute(Map<String, Object> config, WorkflowContext context) {
    Object rawKey = config.get("key");
    if (!(rawKey instanceof String key) || key.isBlank()) {
      return StepResult.failed("set step requires a non-blank 'key' config");
    }
    if (!config.containsKey("value")) {
      return StepResult.failed("set step requires a 'value' config");
    }
    // LinkedHashMap (not Map.of) so a null value is allowed.
    Map<String, Object> output = new LinkedHashMap<>();
    output.put(key, config.get("value"));
    return StepResult.ok(output);
  }
}
