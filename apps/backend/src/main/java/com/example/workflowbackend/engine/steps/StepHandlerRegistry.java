package com.example.workflowbackend.engine.steps;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Maps a step {@code type} to its {@link StepHandler}. All {@link StepHandler} beans are collected by
 * Spring at startup; registering two handlers for the same type fails fast.
 */
@Component
public class StepHandlerRegistry {

  private final Map<String, StepHandler> handlers = new LinkedHashMap<>();

  public StepHandlerRegistry(List<StepHandler> stepHandlers) {
    for (StepHandler handler : stepHandlers) {
      String type = handler.getType();
      StepHandler previous = handlers.putIfAbsent(type, handler);
      if (previous != null) {
        throw new IllegalStateException(
            "Duplicate step handler for type '"
                + type
                + "': "
                + previous.getClass().getName()
                + " and "
                + handler.getClass().getName());
      }
    }
  }

  /** The handler for {@code type}, or empty when no handler is registered. */
  public Optional<StepHandler> find(String type) {
    return Optional.ofNullable(handlers.get(type));
  }

  /** Registered step types, in registration order. */
  public Set<String> types() {
    return Set.copyOf(handlers.keySet());
  }
}
