package com.example.workflowbackend.repository;

import com.example.workflowbackend.domain.ExecutionStepResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionStepResultRepository extends JpaRepository<ExecutionStepResult, Long> {

  List<ExecutionStepResult> findByWorkflowExecutionIdOrderByStepOrderAsc(Long workflowExecutionId);
}
