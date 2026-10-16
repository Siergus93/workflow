package com.example.workflowbackend.engine.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class WorkflowContextTest {

  private final WorkflowContext context = new WorkflowContext();

  @Test
  void storesAndReadsValues() {
    context.put("greeting", "hello");
    context.put("count", 3);

    assertThat(context.get("greeting")).isEqualTo("hello");
    assertThat(context.get("count", Integer.class)).isEqualTo(3);
    assertThat(context.containsKey("count")).isTrue();
  }

  @Test
  void missingKeyReturnsNull() {
    assertThat(context.get("absent")).isNull();
    assertThat(context.get("absent", String.class)).isNull();
    assertThat(context.containsKey("absent")).isFalse();
  }

  @Test
  void typedGetRejectsWrongType() {
    context.put("count", 3);

    assertThatThrownBy(() -> context.get("count", String.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("count");
  }

  @Test
  void asMapIsReadOnlyView() {
    context.put("a", 1);

    assertThat(context.asMap()).containsEntry("a", 1).hasSize(1);
    assertThatThrownBy(() -> context.asMap().put("b", 2))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
