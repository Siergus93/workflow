package com.example.workflowbackend.repository;

import com.example.workflowbackend.domain.ExecutionStepDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionStepDefinitionRepository extends JpaRepository<ExecutionStepDefinition, Long> {
}
