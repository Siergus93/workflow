package com.example.workflowbackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.workflowbackend.domain.ExecutionStep;
import java.util.List;

import com.example.workflowbackend.service.ExecutionStepService;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/execution-steps")
public class ExecutionStepController {

  private final ExecutionStepService executionStepService;

  public ExecutionStepController(ExecutionStepService executionStepService) {
    this.executionStepService = executionStepService;
  }

  @GetMapping
  public List<ExecutionStep> getAllExecutionSteps() {
    return executionStepService.getAllExecutionSteps();
  }

}
