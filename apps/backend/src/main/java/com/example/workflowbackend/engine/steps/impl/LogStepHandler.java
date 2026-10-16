package com.example.workflowbackend.engine.steps.impl;

import com.example.workflowbackend.engine.context.WorkflowContext;
import com.example.workflowbackend.engine.steps.StepHandler;
import com.example.workflowbackend.engine.steps.StepResult;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Logs a configured message. Config: {@code message} (string, required). */
@Component
public class LogStepHandler implements StepHandler {

  private static final Logger log = LoggerFactory.getLogger(LogStepHandler.class);
  private static final String TYPE = "log";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public StepResult execute(Map<String, Object> config, WorkflowContext context) {
    Object message = config.get("message");
    if (message == null) {
      return StepResult.failed("log step requires a 'message' config");
    }
    String text = message.toString();
    log.info("{}", text);
    return StepResult.ok(Map.of("message", text));
  }
}
