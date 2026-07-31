package com.example.workflowbackend.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "execution_step_results")
public class ExecutionStepResult {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "workflow_execution_id", nullable = false)
  private WorkflowExecution workflowExecution;

  private String stepName;

  @Enumerated(EnumType.STRING)
  private ExecutionStatus status = ExecutionStatus.PENDING;

  @Column(nullable = false, updatable = false)
  private LocalDateTime startedAt;

  private LocalDateTime finishedAt;

  private String output;

  private String errorMessage;
}
