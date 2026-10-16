package com.example.workflowbackend.engine.definition;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Deserialized representation of a workflow JSON document (see
 * {@code docs/workflow-json-schema.md}). Separate from the JPA entities, which model persisted
 * execution state.
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkflowDefinition {

  private String name;
  private String description;
  private List<StepDefinition> steps;
}
