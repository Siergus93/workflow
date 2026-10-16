package com.example.workflowbackend.engine.context;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mutable, run-scoped state shared between steps. Outputs written by one step are readable by later
 * steps. Not thread-safe; the engine executes steps sequentially.
 */
public class WorkflowContext {

  private final Map<String, Object> values = new LinkedHashMap<>();

  public void put(String key, Object value) {
    values.put(key, value);
  }

  public Object get(String key) {
    return values.get(key);
  }

  /** Returns the value cast to {@code type}, or {@code null} when absent. */
  public <T> T get(String key, Class<T> type) {
    Object value = values.get(key);
    if (value == null) {
      return null;
    }
    if (!type.isInstance(value)) {
      throw new IllegalStateException(
          "Context value '"
              + key
              + "' is a "
              + value.getClass().getName()
              + ", not a "
              + type.getName());
    }
    return type.cast(value);
  }

  public boolean containsKey(String key) {
    return values.containsKey(key);
  }

  /** Read-only view of the current context, mainly for serialization into step output. */
  public Map<String, Object> asMap() {
    return Collections.unmodifiableMap(values);
  }
}
