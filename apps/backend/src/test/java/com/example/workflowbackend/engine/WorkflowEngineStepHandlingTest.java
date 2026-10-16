package com.example.workflowbackend.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.workflowbackend.domain.ExecutionStatus;
import com.example.workflowbackend.domain.ExecutionStepResult;
import com.example.workflowbackend.domain.WorkflowExecution;
import com.example.workflowbackend.engine.context.WorkflowContext;
import com.example.workflowbackend.engine.steps.StepHandler;
import com.example.workflowbackend.engine.steps.StepResult;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Phase 6: engine behavior against purpose-built handlers — shared-context propagation and the
 * handler-throws failure path (seeds Issue 11).
 */
@SpringBootTest
@Import(WorkflowEngineStepHandlingTest.TestHandlers.class)
class WorkflowEngineStepHandlingTest {

  @Autowired private WorkflowEngine engine;

  @Test
  void passesStepOutputToLaterSteps() {
    WorkflowExecution execution = engine.execute("context-probe");

    assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
    assertThat(execution.getStepResults()).hasSize(2);

    ExecutionStepResult seed = execution.getStepResults().get(0);
    ExecutionStepResult read = execution.getStepResults().get(1);
    assertThat(seed.getStepName()).isEqualTo("seed");
    assertThat(read.getStepName()).isEqualTo("read");
    // 'seed' (log) wrote "message"; 'read' (capture) read it back from the shared context.
    assertThat(read.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
    assertThat(read.getOutput()).contains("seeded-value");
  }

  @Test
  void recordsFailureWhenHandlerThrows() {
    WorkflowExecution execution = engine.execute("throwing-step");

    assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.FAILED);
    assertThat(execution.getErrorMessage()).contains("kaboom");
    assertThat(execution.getFinishedAt()).isNotNull();
    assertThat(execution.getStepResults()).hasSize(1);

    ExecutionStepResult boom = execution.getStepResults().get(0);
    assertThat(boom.getStatus()).isEqualTo(ExecutionStatus.FAILED);
    assertThat(boom.getErrorMessage()).contains("kaboom");
  }

  @TestConfiguration
  static class TestHandlers {

    /** Reads {@code key} from the shared context and echoes it back as output. */
    @Bean
    StepHandler captureHandler() {
      return new StepHandler() {
        @Override
        public String getType() {
          return "capture";
        }

        @Override
        public StepResult execute(Map<String, Object> config, WorkflowContext context) {
          String key = String.valueOf(config.get("key"));
          Object value = context.get(key);
          if (value == null) {
            return StepResult.failed("context key not found: " + key);
          }
          return StepResult.ok(Map.of("captured", value));
        }
      };
    }

    /** Always throws, to exercise the engine's exception handling. */
    @Bean
    StepHandler explodeHandler() {
      return new StepHandler() {
        @Override
        public String getType() {
          return "explode";
        }

        @Override
        public StepResult execute(Map<String, Object> config, WorkflowContext context) {
          throw new IllegalStateException("kaboom");
        }
      };
    }
  }
}
