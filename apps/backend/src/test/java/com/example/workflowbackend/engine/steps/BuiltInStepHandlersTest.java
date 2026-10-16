package com.example.workflowbackend.engine.steps;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Verifies the built-in handlers are discovered by Spring and registered by type. */
@SpringBootTest
class BuiltInStepHandlersTest {

  @Autowired private StepHandlerRegistry registry;

  @Test
  void registersLogAndDelay() {
    assertThat(registry.find("log")).isPresent();
    assertThat(registry.find("delay")).isPresent();
    assertThat(registry.types()).contains("log", "delay");
  }
}
