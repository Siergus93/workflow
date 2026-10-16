package com.example.workflowbackend.engine.steps.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.workflowbackend.engine.context.WorkflowContext;
import com.example.workflowbackend.engine.steps.StepResult;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LogStepHandlerTest {

  private final LogStepHandler handler = new LogStepHandler();

  @Test
  void declaresLogType() {
    assertThat(handler.getType()).isEqualTo("log");
  }

  @Test
  void logsMessageAndReturnsOutput() {
    StepResult result = handler.execute(Map.of("message", "hello"), new WorkflowContext());

    assertThat(result.success()).isTrue();
    assertThat(result.output()).isEqualTo(Map.of("message", "hello"));
    assertThat(result.errorMessage()).isNull();
  }

  @Test
  void failsWhenMessageMissing() {
    StepResult result = handler.execute(Map.of(), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("message");
  }
}
