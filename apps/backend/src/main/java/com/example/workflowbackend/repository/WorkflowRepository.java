package com.example.workflowbackend.repository;

import com.example.workflowbackend.domain.Workflow;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

  Optional<Workflow> findByName(String name);
}
