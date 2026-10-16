package com.example.workflowbackend.engine.steps;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StepResultTest {

  @Test
  void okCarriesOutputWithoutError() {
    StepResult result = StepResult.ok("done");

    assertThat(result.success()).isTrue();
    assertThat(result.output()).isEqualTo("done");
    assertThat(result.errorMessage()).isNull();
  }

  @Test
  void failedCarriesMessageWithoutOutput() {
    StepResult result = StepResult.failed("boom");

    assertThat(result.success()).isFalse();
    assertThat(result.output()).isNull();
    assertThat(result.errorMessage()).isEqualTo("boom");
  }
}
