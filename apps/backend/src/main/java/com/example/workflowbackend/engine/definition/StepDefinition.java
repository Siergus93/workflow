package com.example.workflowbackend.engine.definition;

import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A single step within a {@link WorkflowDefinition}. Order is the containing list's index. */
@Getter
@Setter
@NoArgsConstructor
public class StepDefinition {

  /** Optional identifier; the engine falls back to {@code <type>#<index>} when absent. */
  private String name;

  private String type;
  private Map<String, Object> config;
}
