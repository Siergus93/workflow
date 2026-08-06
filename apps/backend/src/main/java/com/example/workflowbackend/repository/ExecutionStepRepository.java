package com.example.workflowbackend.repository;

import com.example.workflowbackend.domain.ExecutionStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionStepRepository extends JpaRepository<ExecutionStep, Long> {
}
