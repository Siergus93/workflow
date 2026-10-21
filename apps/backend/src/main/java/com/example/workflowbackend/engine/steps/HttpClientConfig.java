package com.example.workflowbackend.engine.steps;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the shared {@link HttpClient} used by HTTP step handlers. Kept as a bean so tests can
 * substitute a client, and so connection pooling is shared across steps.
 */
@Configuration
public class HttpClientConfig {

  @Bean
  public HttpClient workflowHttpClient() {
    return HttpClient.newHttpClient();
  }
}
