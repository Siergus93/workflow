package com.example.workflowbackend.engine.loader;

import com.example.workflowbackend.engine.definition.StepDefinition;
import com.example.workflowbackend.engine.definition.WorkflowDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Loads workflow definitions from {@code classpath*:workflows/*.json} (all classpath roots, so test
 * fixtures alongside main resources are visible).
 *
 * <p>A workflow is looked up by the {@code name} field inside the JSON document, not by file name,
 * so {@code workflows/sample.json} is retrievable as {@code "sample-workflow"}.
 */
@Component
public class WorkflowLoader {

  private static final String WORKFLOW_PATTERN = "classpath*:workflows/*.json";

  private final ObjectMapper mapper;
  private final ResourcePatternResolver resolver;

  @Autowired
  public WorkflowLoader(ObjectMapper mapper) {
    this.mapper = mapper;
    this.resolver = new PathMatchingResourcePatternResolver();
  }

  public WorkflowDefinition load(String workflowName) {
    for (Resource resource : listDefinitions()) {
      WorkflowDefinition definition = read(resource);
      if (workflowName.equals(definition.getName())) {
        validate(definition, resource);
        return definition;
      }
    }
    throw new WorkflowLoadException("Workflow definition not found: " + workflowName);
  }

  private Resource[] listDefinitions() {
    try {
      return resolver.getResources(WORKFLOW_PATTERN);
    } catch (IOException e) {
      throw new WorkflowLoadException("Cannot list workflow definitions", e);
    }
  }

  private WorkflowDefinition read(Resource resource) {
    try (InputStream in = resource.getInputStream()) {
      return mapper.readValue(in, WorkflowDefinition.class);
    } catch (IOException e) {
      throw new WorkflowLoadException("Failed to read workflow definition: " + resource, e);
    }
  }

  private void validate(WorkflowDefinition definition, Resource resource) {
    if (isBlank(definition.getName())) {
      throw new WorkflowLoadException("Workflow definition has no name: " + resource);
    }
    List<StepDefinition> steps = definition.getSteps();
    if (steps == null || steps.isEmpty()) {
      throw new WorkflowLoadException(
          "Workflow '" + definition.getName() + "' must declare at least one step");
    }
    for (StepDefinition step : steps) {
      if (step == null || isBlank(step.getType())) {
        throw new WorkflowLoadException(
            "Workflow '" + definition.getName() + "' has a step with no type");
      }
      if (step.getConfig() == null) {
        throw new WorkflowLoadException(
            "Workflow '" + definition.getName() + "' step '" + step.getName() + "' has no config");
      }
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
