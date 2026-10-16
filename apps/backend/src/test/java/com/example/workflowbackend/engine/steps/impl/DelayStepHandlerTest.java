package com.example.workflowbackend.engine.steps.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.workflowbackend.engine.context.WorkflowContext;
import com.example.workflowbackend.engine.steps.StepResult;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DelayStepHandlerTest {

  private final DelayStepHandler handler = new DelayStepHandler();

  @Test
  void declaresDelayType() {
    assertThat(handler.getType()).isEqualTo("delay");
  }

  @Test
  void delaysAndReturnsOutput() {
    StepResult result = handler.execute(Map.of("seconds", 0), new WorkflowContext());

    assertThat(result.success()).isTrue();
    assertThat(result.output()).isEqualTo(Map.of("delayedSeconds", 0L));
  }

  @Test
  void failsWhenSecondsMissing() {
    StepResult result = handler.execute(Map.of(), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("seconds");
  }

  @Test
  void failsOnNegativeSeconds() {
    StepResult result = handler.execute(Map.of("seconds", -1), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("negative");
  }
}
