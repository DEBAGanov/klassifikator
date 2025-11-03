/**
 * @file: WebClientConfig.java
 * @description: Configuration for WebClient to call other services
 * @dependencies: Spring WebFlux
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

