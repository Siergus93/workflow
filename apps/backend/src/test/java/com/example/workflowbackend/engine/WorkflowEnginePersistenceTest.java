package com.example.workflowbackend.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.workflowbackend.domain.ExecutionStatus;
import com.example.workflowbackend.domain.ExecutionStepResult;
import com.example.workflowbackend.domain.Workflow;
import com.example.workflowbackend.domain.WorkflowExecution;
import com.example.workflowbackend.repository.ExecutionStepResultRepository;
import com.example.workflowbackend.repository.WorkflowExecutionRepository;
import com.example.workflowbackend.repository.WorkflowRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Phase 5 verification: run a workflow, then re-read the persisted rows from the database (not the
 * in-memory return value) and assert the recorded state.
 */
@SpringBootTest
class WorkflowEnginePersistenceTest {

  @Autowired private WorkflowEngine engine;
  @Autowired private WorkflowRepository workflowRepository;
  @Autowired private WorkflowExecutionRepository executionRepository;
  @Autowired private ExecutionStepResultRepository stepResultRepository;

  @Test
  void persistsExecutionAndOrderedStepResults() {
    WorkflowExecution execution = engine.execute("sample-workflow");

    WorkflowExecution reloaded = executionRepository.findById(execution.getId()).orElseThrow();
    assertThat(reloaded.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
    assertThat(reloaded.getStartedAt()).isNotNull();
    assertThat(reloaded.getFinishedAt()).isNotNull();
    assertThat(reloaded.getErrorMessage()).isNull();

    List<ExecutionStepResult> steps =
        stepResultRepository.findByWorkflowExecutionIdOrderByStepOrderAsc(reloaded.getId());
    assertThat(steps).hasSize(3);
    assertThat(steps).extracting(ExecutionStepResult::getStepOrder).containsExactly(0, 1, 2);
    assertThat(steps)
        .extracting(ExecutionStepResult::getStepName)
        .containsExactly("start", "wait", "finish");
    assertThat(steps)
        .allMatch(step -> step.getStatus() == ExecutionStatus.COMPLETED)
        .allMatch(step -> step.getStartedAt() != null && step.getFinishedAt() != null)
        .allMatch(step -> step.getOutput() != null);
  }

  @Test
  void createsWorkflowRowWithCreatedAt() {
    engine.execute("sample-workflow");

    Workflow workflow = workflowRepository.findByName("sample-workflow").orElseThrow();
    assertThat(workflow.getId()).isNotNull();
    assertThat(workflow.getCreatedAt()).isNotNull();
    assertThat(workflow.getDescription()).isEqualTo("A minimal sample workflow for local testing");
  }
}
