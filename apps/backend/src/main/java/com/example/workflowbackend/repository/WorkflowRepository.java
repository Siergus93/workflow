package com.example.workflowbackend.repository;

import com.example.workflowbackend.domain.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
}
