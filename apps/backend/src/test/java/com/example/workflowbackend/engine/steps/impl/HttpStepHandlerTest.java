package com.example.workflowbackend.engine.steps.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.workflowbackend.engine.context.WorkflowContext;
import com.example.workflowbackend.engine.steps.StepResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link HttpStepHandler} against a local in-JVM server, so the tests need no network
 * access and stay deterministic.
 */
class HttpStepHandlerTest {

  private HttpServer server;
  private ExecutorService executor;
  private HttpStepHandler handler;
  private String baseUrl;

  @BeforeEach
  void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    executor = Executors.newCachedThreadPool();
    server.setExecutor(executor);

    server.createContext("/ok", exchange -> respond(exchange, 200, "hello"));
    server.createContext(
        "/echo",
        exchange -> {
          String header = exchange.getRequestHeaders().getFirst("X-Test");
          respond(
              exchange,
              200,
              exchange.getRequestMethod() + ":" + readBody(exchange) + ":" + header);
        });
    server.createContext("/boom", exchange -> respond(exchange, 500, "server error"));
    server.createContext(
        "/slow",
        exchange -> {
          sleep(2000);
          respond(exchange, 200, "late");
        });
    server.start();

    baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    handler = new HttpStepHandler(HttpClient.newHttpClient());
  }

  @AfterEach
  void stopServer() {
    server.stop(0);
    executor.shutdownNow();
  }

  @Test
  void declaresHttpType() {
    assertThat(handler.getType()).isEqualTo("http");
  }

  @Test
  void getReturnsStatusBodyAndUrl() {
    StepResult result = handler.execute(Map.of("url", baseUrl + "/ok"), new WorkflowContext());

    assertThat(result.success()).isTrue();
    assertThat(result.output())
        .isEqualTo(Map.of("status", 200, "body", "hello", "url", baseUrl + "/ok"));
    assertThat(result.errorMessage()).isNull();
  }

  @Test
  void postSendsBodyAndHeaders() {
    StepResult result =
        handler.execute(
            Map.of(
                "url", baseUrl + "/echo",
                "method", "POST",
                "body", "payload",
                "headers", Map.of("X-Test", "abc")),
            new WorkflowContext());

    assertThat(result.success()).isTrue();
    assertThat(((Map<?, ?>) result.output()).get("status")).isEqualTo(200);
    assertThat(((Map<?, ?>) result.output()).get("body")).isEqualTo("POST:payload:abc");
  }

  @Test
  void failsOnNonSuccessStatus() {
    StepResult result = handler.execute(Map.of("url", baseUrl + "/boom"), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("500");
  }

  @Test
  void failsWhenRequestTimesOut() {
    StepResult result =
        handler.execute(
            Map.of("url", baseUrl + "/slow", "timeoutSeconds", 1), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("failed");
  }

  @Test
  void failsWhenServerUnreachable() throws IOException {
    int closedPort;
    try (ServerSocket socket = new ServerSocket(0)) {
      closedPort = socket.getLocalPort();
    }

    StepResult result =
        handler.execute(Map.of("url", "http://127.0.0.1:" + closedPort), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("failed");
  }

  @Test
  void failsWhenUrlMissing() {
    StepResult result = handler.execute(Map.of(), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("url");
  }

  @Test
  void failsWhenSchemeUnsupported() {
    StepResult result =
        handler.execute(Map.of("url", "ftp://example.com"), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("http");
  }

  @Test
  void failsWhenMethodUnsupported() {
    StepResult result =
        handler.execute(Map.of("url", baseUrl + "/ok", "method", "DELETE"), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("method");
  }

  @Test
  void failsWhenTimeoutInvalid() {
    StepResult result =
        handler.execute(
            Map.of("url", baseUrl + "/ok", "timeoutSeconds", 0), new WorkflowContext());

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).contains("timeoutSeconds");
  }

  private static String readBody(HttpExchange exchange) throws IOException {
    return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
  }

  private static void respond(HttpExchange exchange, int status, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(bytes);
    }
  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
