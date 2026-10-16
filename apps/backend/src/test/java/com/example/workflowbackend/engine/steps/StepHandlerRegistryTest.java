package com.example.workflowbackend.engine.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.workflowbackend.engine.context.WorkflowContext;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StepHandlerRegistryTest {

  @Test
  void resolvesRegisteredHandlerByType() {
    StubHandler handler = new StubHandler("log");

    StepHandlerRegistry registry = new StepHandlerRegistry(List.of(handler));

    assertThat(registry.find("log")).isPresent();
    assertThat(registry.find("log").orElseThrow()).isSameAs(handler);
    assertThat(registry.types()).containsExactly("log");
  }

  @Test
  void unknownTypeReturnsEmpty() {
    StepHandlerRegistry registry = new StepHandlerRegistry(List.of());

    assertThat(registry.find("nope")).isEmpty();
    assertThat(registry.types()).isEmpty();
  }

  @Test
  void rejectsDuplicateTypes() {
    StepHandler first = new StubHandler("log");
    StepHandler second = new StubHandler("log");

    assertThatThrownBy(() -> new StepHandlerRegistry(List.of(first, second)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("log");
  }

  private static final class StubHandler implements StepHandler {

    private final String type;

    private StubHandler(String type) {
      this.type = type;
    }

    @Override
    public String getType() {
      return type;
    }

    @Override
    public StepResult execute(Map<String, Object> config, WorkflowContext context) {
      return StepResult.ok(null);
    }
  }
}
