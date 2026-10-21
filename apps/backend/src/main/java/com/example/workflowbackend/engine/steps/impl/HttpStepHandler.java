package com.example.workflowbackend.engine.steps.impl;

import com.example.workflowbackend.engine.context.WorkflowContext;
import com.example.workflowbackend.engine.steps.StepHandler;
import com.example.workflowbackend.engine.steps.StepResult;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Calls an HTTP endpoint and exposes the response to later steps.
 *
 * <p>Config: {@code url} (string, required, {@code http}/{@code https}); {@code method}
 * ({@code GET} or {@code POST}, default {@code GET}); {@code headers} (object of string values,
 * optional); {@code body} (string, optional, sent for {@code POST}); {@code timeoutSeconds}
 * (number, default {@code 10}, must be positive).
 *
 * <p>A 2xx response succeeds and yields {@code {status, body, url}}. Any other status, an invalid
 * config, a connection error, or a timeout fails the step with a clear message.
 */
@Component
public class HttpStepHandler implements StepHandler {

  private static final String TYPE = "http";
  private static final long DEFAULT_TIMEOUT_SECONDS = 10L;

  private final HttpClient client;

  public HttpStepHandler(HttpClient client) {
    this.client = client;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public StepResult execute(Map<String, Object> config, WorkflowContext context) {
    Object rawUrl = config.get("url");
    if (!(rawUrl instanceof String url) || url.isBlank()) {
      return StepResult.failed("http step requires a non-blank 'url' config");
    }
    URI uri;
    try {
      uri = URI.create(url);
    } catch (IllegalArgumentException e) {
      return StepResult.failed("http step 'url' is not a valid URI: " + url);
    }
    String scheme = uri.getScheme();
    if (scheme == null
        || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
      return StepResult.failed("http step 'url' must use http or https: " + url);
    }

    Object rawMethod = config.get("method");
    String method = rawMethod == null ? "GET" : rawMethod.toString().trim().toUpperCase(Locale.ROOT);
    if (!method.equals("GET") && !method.equals("POST")) {
      return StepResult.failed("http step 'method' must be GET or POST, but was '" + rawMethod + "'");
    }

    Duration timeout = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS);
    Object rawTimeout = config.get("timeoutSeconds");
    if (rawTimeout != null) {
      if (!(rawTimeout instanceof Number number)) {
        return StepResult.failed("http step 'timeoutSeconds' must be a number");
      }
      long seconds = number.longValue();
      if (seconds <= 0) {
        return StepResult.failed("http step 'timeoutSeconds' must be greater than zero");
      }
      timeout = Duration.ofSeconds(seconds);
    }

    HttpRequest.Builder request = HttpRequest.newBuilder(uri).timeout(timeout);
    Object rawHeaders = config.get("headers");
    if (rawHeaders != null) {
      if (!(rawHeaders instanceof Map<?, ?> headers)) {
        return StepResult.failed("http step 'headers' must be an object");
      }
      for (Map.Entry<?, ?> header : headers.entrySet()) {
        request.header(String.valueOf(header.getKey()), String.valueOf(header.getValue()));
      }
    }

    HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.noBody();
    Object rawBody = config.get("body");
    if (rawBody != null) {
      body = HttpRequest.BodyPublishers.ofString(String.valueOf(rawBody));
    }
    request.method(method, body);

    try {
      HttpResponse<String> response =
          client.send(request.build(), HttpResponse.BodyHandlers.ofString());
      int status = response.statusCode();
      if (status < 200 || status >= 300) {
        return StepResult.failed(
            "http step '" + url + "' returned status " + status + ": " + response.body());
      }
      Map<String, Object> output = new LinkedHashMap<>();
      output.put("status", status);
      output.put("body", response.body());
      output.put("url", url);
      return StepResult.ok(output);
    } catch (IOException e) {
      return StepResult.failed("http step '" + url + "' failed: " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return StepResult.failed("http step '" + url + "' was interrupted");
    }
  }
}
