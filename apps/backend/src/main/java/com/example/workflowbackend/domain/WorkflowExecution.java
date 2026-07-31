package com.example.workflowbackend.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "workflow_executions")
public class WorkflowExecution {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "workflow_id", nullable = false)
  private Workflow workflow;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ExecutionStatus status = ExecutionStatus.PENDING;

  @Column(nullable = false, updatable = false)
  private LocalDateTime startedAt = LocalDateTime.now();

  private LocalDateTime finishedAt;

  private String errorMessage;

  @OneToMany(mappedBy = "workflowExecution", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ExecutionStepResult> stepResults = new ArrayList<>();

  public WorkflowExecution(Workflow workflow) {
    this.workflow = workflow;
  }
}
