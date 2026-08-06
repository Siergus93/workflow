package com.example.workflowbackend.service;

import com.example.workflowbackend.repository.ExecutionStepRepository;
import com.example.workflowbackend.domain.ExecutionStep;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExecutionStepService {
  private final ExecutionStepRepository executionStepRepository;

  public ExecutionStepService(ExecutionStepRepository executionStepRepository) {
    this.executionStepRepository = executionStepRepository;
  }

  public ExecutionStep saveExecutionStep(ExecutionStep executionStep) {
    return executionStepRepository.save(executionStep);
  }

  public List<ExecutionStep> getAllExecutionSteps() {
    return executionStepRepository.findAll();
  }
}
