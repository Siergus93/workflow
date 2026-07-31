package com.example.workflowbackend.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "execution_steps")
public class ExecutionStep {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "workflow_id", nullable = false)
  private Workflow workflow;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String type;

  @Column(length = 4000)
  private String config;
}
