package com.example.workflowbackend.engine.steps.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.workflowbackend.engine.context.WorkflowContext;
import com.example.workflowbackend.engine.steps.StepResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SetStepHandlerTest {

  private final SetStepHandler handler = new SetStepHandler();

  @Test
  void declaresSetType() {
    assertThat(handler.getType()).isEqualTo("set");
  }

  @Test
  void returnsValueUnderConfiguredKey() {
    StepResult result =
        handler.execute(Map.of("key", "customer", "value", "acme"), new WorkflowContext());

    assertThat(result.success()).isTrue();
    assertThat(result.output()).isEqualTo(Map.of("customer", "acme"));
    assertThat(result.errorMessage()).isNull();
  }

  @Test
  void allowsNullValue() {
    Map<String, Object> config = new HashMap<>();
    config.put("key", "maybe");
    config.put("value", null);

    StepResult result = handler.execute(config, new WorkflowContext());

    assertThat(result.success()).isTrue();
    assertThat(result.output()).isInstanceOf(Map.class);
    assertThat(((Map<?, ?>) result.output()).get("maybe")).isNull();
  }

  @Test
  void passesObjectValueThroughUnchanged() {
    Map<String, Object> value = Map.of("total", 42);

    StepResult result =
        handler.execute(Map.of("key", "invoice", "value", value), new WorkflowContext());

    assertThat(result.success()).isTrue();
    assertThat(result.output()).isEqualTo(Map.of("invoice", value));
  }

  @Test
  void failsWhenKeyMissing() {
    StepResult result = handler.execute(Map.of("value", "x"), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("key");
  }

  @Test
  void failsWhenKeyBlank() {
    StepResult result =
        handler.execute(Map.of("key", "  ", "value", "x"), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("key");
  }

  @Test
  void failsWhenValueMissing() {
    StepResult result = handler.execute(Map.of("key", "x"), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("value");
  }
}
