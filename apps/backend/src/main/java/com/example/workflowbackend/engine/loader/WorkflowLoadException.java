package com.example.workflowbackend.engine.loader;

public class WorkflowLoadException extends RuntimeException {

  public WorkflowLoadException(String message) {
    super(message);
  }

  public WorkflowLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
