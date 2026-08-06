package com.example.workflowbackend.repository;

import com.example.workflowbackend.domain.ExecutionStepResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionStepResultRepository extends JpaRepository<ExecutionStepResult, Long> {
}
