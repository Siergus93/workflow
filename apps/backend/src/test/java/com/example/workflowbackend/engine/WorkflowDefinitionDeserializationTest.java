package com.example.workflowbackend.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.workflowbackend.engine.definition.WorkflowDefinition;
import com.example.workflowbackend.engine.loader.WorkflowLoadException;
import com.example.workflowbackend.engine.loader.WorkflowLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WorkflowDefinitionDeserializationTest {

  @Autowired private WorkflowLoader loader;

  @Test
  void deserializesSampleWorkflow() {
    WorkflowDefinition def = loader.load("sample-workflow");
    assertThat(def.getName()).isEqualTo("sample-workflow");
    assertThat(def.getSteps()).hasSize(3);
    assertThat(def.getSteps().get(0).getName()).isEqualTo("start");
    assertThat(def.getSteps().get(0).getType()).isEqualTo("log");
    assertThat(def.getSteps().get(1).getName()).isEqualTo("wait");
    assertThat(def.getSteps().get(2).getName()).isEqualTo("finish");
  }

  @Test
  void throwsOnUnknownWorkflow() {
    assertThatThrownBy(() -> loader.load("missing"))
        .isInstanceOf(WorkflowLoadException.class);
  }
}
