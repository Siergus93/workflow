package com.example.workflowbackend.engine;

import com.example.workflowbackend.domain.ExecutionStatus;
import com.example.workflowbackend.domain.ExecutionStepResult;
import com.example.workflowbackend.domain.Workflow;
import com.example.workflowbackend.domain.WorkflowExecution;
import com.example.workflowbackend.engine.context.WorkflowContext;
import com.example.workflowbackend.engine.definition.StepDefinition;
import com.example.workflowbackend.engine.definition.WorkflowDefinition;
import com.example.workflowbackend.engine.loader.WorkflowLoader;
import com.example.workflowbackend.engine.steps.StepHandler;
import com.example.workflowbackend.engine.steps.StepHandlerRegistry;
import com.example.workflowbackend.engine.steps.StepResult;
import com.example.workflowbackend.repository.ExecutionStepResultRepository;
import com.example.workflowbackend.repository.WorkflowExecutionRepository;
import com.example.workflowbackend.repository.WorkflowRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

/**
 * Loads a workflow definition, executes its steps sequentially against a shared
 * {@link WorkflowContext}, and persists the execution and per-step results.
 *
 * <p>Intentionally <em>not</em> {@code @Transactional}: each repository {@code save} runs in its own
 * transaction, so a failed step's result and the FAILED execution survive even when a later handler
 * throws (plan decision #5).
 */
@Service
public class WorkflowEngine {

  private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  private final WorkflowLoader loader;
  private final WorkflowRepository workflowRepository;
  private final WorkflowExecutionRepository executionRepository;
  private final ExecutionStepResultRepository stepResultRepository;
  private final StepHandlerRegistry handlerRegistry;
  private final ObjectMapper objectMapper;

  public WorkflowEngine(
      WorkflowLoader loader,
      WorkflowRepository workflowRepository,
      WorkflowExecutionRepository executionRepository,
      ExecutionStepResultRepository stepResultRepository,
      StepHandlerRegistry handlerRegistry,
      ObjectMapper objectMapper) {
    this.loader = loader;
    this.workflowRepository = workflowRepository;
    this.executionRepository = executionRepository;
    this.stepResultRepository = stepResultRepository;
    this.handlerRegistry = handlerRegistry;
    this.objectMapper = objectMapper;
  }

  /**
   * Runs the named workflow end to end.
   *
   * @param workflowName the {@code name} field of a workflow JSON document under {@code workflows/}
   * @return the persisted execution, with its step results populated and ordered
   */
  public WorkflowExecution execute(String workflowName) {
    WorkflowDefinition definition = loader.load(workflowName);
    Workflow workflow = findOrCreateWorkflow(definition);

    WorkflowExecution execution = new WorkflowExecution(workflow);
    execution.setStatus(ExecutionStatus.RUNNING);
    execution = executionRepository.save(execution);

    WorkflowContext context = new WorkflowContext();
    List<ExecutionStepResult> results = new ArrayList<>();

    List<StepDefinition> steps = definition.getSteps();
    for (int index = 0; index < steps.size(); index++) {
      StepDefinition step = steps.get(index);
      String stepName = resolveStepName(step, index);

      ExecutionStepResult result = startStep(execution, stepName, index);
      results.add(result);

      Optional<StepHandler> handler = handlerRegistry.find(step.getType());
      if (handler.isEmpty()) {
        return fail(execution, result, results, "Unsupported step type: " + step.getType());
      }

      StepResult stepResult;
      try {
        stepResult = handler.get().execute(step.getConfig(), context);
      } catch (RuntimeException e) {
        log.warn("Step '{}' of workflow '{}' threw", stepName, workflowName, e);
        stepResult = StepResult.failed("Step '" + stepName + "' failed: " + e.getMessage());
      }

      if (!stepResult.success()) {
        String error =
            stepResult.errorMessage() != null ? stepResult.errorMessage() : "Step failed";
        return fail(execution, result, results, error);
      }

      completeStep(result, stepResult.output());
      mergeIntoContext(context, stepName, stepResult.output());
    }

    execution.setStatus(ExecutionStatus.COMPLETED);
    execution.setFinishedAt(LocalDateTime.now());
    execution = executionRepository.save(execution);
    execution.setStepResults(results);
    return execution;
  }

  private Workflow findOrCreateWorkflow(WorkflowDefinition definition) {
    return workflowRepository
        .findByName(definition.getName())
        .orElseGet(
            () -> {
              Workflow workflow = new Workflow();
              workflow.setName(definition.getName());
              workflow.setDescription(definition.getDescription());
              // createdAt is nullable = false with no default; set it explicitly (plan decision #3).
              workflow.setCreatedAt(LocalDateTime.now());
              return workflowRepository.save(workflow);
            });
  }

  private ExecutionStepResult startStep(WorkflowExecution execution, String stepName, int order) {
    ExecutionStepResult result = new ExecutionStepResult();
    result.setWorkflowExecution(execution);
    result.setStepName(stepName);
    result.setStepOrder(order);
    result.setStatus(ExecutionStatus.RUNNING);
    result.setStartedAt(LocalDateTime.now());
    return stepResultRepository.save(result);
  }

  private void completeStep(ExecutionStepResult result, Object output) {
    result.setStatus(ExecutionStatus.COMPLETED);
    result.setFinishedAt(LocalDateTime.now());
    result.setOutput(serialize(output));
    stepResultRepository.save(result);
  }

  private WorkflowExecution fail(
      WorkflowExecution execution,
      ExecutionStepResult result,
      List<ExecutionStepResult> results,
      String error) {
    result.setStatus(ExecutionStatus.FAILED);
    result.setErrorMessage(error);
    result.setFinishedAt(LocalDateTime.now());
    stepResultRepository.save(result);

    execution.setStatus(ExecutionStatus.FAILED);
    execution.setErrorMessage(error);
    execution.setFinishedAt(LocalDateTime.now());
    executionRepository.save(execution);

    execution.setStepResults(results);
    return execution;
  }

  private void mergeIntoContext(WorkflowContext context, String stepName, Object output) {
    if (output == null) {
      return;
    }
    if (output instanceof Map<?, ?> map) {
      map.forEach((key, value) -> context.put(String.valueOf(key), value));
    } else {
      context.put(stepName, output);
    }
  }

  private String resolveStepName(StepDefinition step, int index) {
    String name = step.getName();
    if (name != null && !name.isBlank()) {
      return name;
    }
    return step.getType() + "#" + (index + 1);
  }

  private String serialize(Object output) {
    if (output == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(output);
    } catch (RuntimeException e) {
      log.warn("Could not serialize step output as JSON; falling back to toString()", e);
      return String.valueOf(output);
    }
  }
}
