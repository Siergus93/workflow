package com.example.workflowbackend.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.workflowbackend.domain.ExecutionStatus;
import com.example.workflowbackend.domain.WorkflowExecution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Issue 5 acceptance criteria captured before implementation: these are red until the engine lands,
 * then become the seed for Issue 11. See {@code docs/issue-5-engine-plan.md}.
 */
@SpringBootTest
class WorkflowEngineTest {

  @Autowired
  private WorkflowEngine engine;

  @Test
  void runsSampleWorkflowToCompletion() {
    WorkflowExecution execution = engine.execute("sample-workflow");

    assertThat(execution.getId()).isNotNull();
    assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
    assertThat(execution.getStartedAt()).isNotNull();
    assertThat(execution.getFinishedAt()).isNotNull();
    assertThat(execution.getErrorMessage()).isNull();

    // sample.json has three steps; they run in JSON order.
    assertThat(execution.getStepResults()).hasSize(3);
    assertThat(execution.getStepResults())
        .allMatch(result -> result.getStatus() == ExecutionStatus.COMPLETED);
  }

  @Test
  void stopsAndRecordsFailureForUnsupportedStepType() {
    WorkflowExecution execution = engine.execute("unsupported-step");

    assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.FAILED);
    assertThat(execution.getErrorMessage()).contains("Unsupported step type");
    assertThat(execution.getStepResults())
        .anyMatch(result -> result.getStatus() == ExecutionStatus.FAILED);
  }
}
