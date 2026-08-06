package com.example.workflowbackend.repository;

import com.example.workflowbackend.domain.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, Long> {
}
