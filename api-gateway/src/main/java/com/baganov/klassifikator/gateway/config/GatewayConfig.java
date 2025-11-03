/**
 * @file: GatewayConfig.java
 * @description: Configuration for API Gateway routes
 * @dependencies: Spring Cloud Gateway
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Landing Service routes
                .route("landing-service", r -> r
                        .path("/api/v1/landings/**", "/api/v1/organizations/**")
                        .uri("http://localhost:8081"))

                // Content Service routes
                .route("content-service", r -> r
                        .path("/api/v1/content/**", "/api/v1/products/**", "/api/v1/promotions/**")
                        .uri("http://localhost:8082"))

                // Template Service routes
                .route("template-service", r -> r
                        .path("/api/v1/templates/**")
                        .uri("http://localhost:8083"))

                // Media Service routes
                .route("media-service", r -> r
                        .path("/api/v1/media/**")
                        .uri("http://localhost:8084"))

                // Integration Service routes
                .route("integration-service", r -> r
                        .path("/api/v1/integration/**")
                        .uri("http://localhost:8085"))

                // Order Service routes
                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .uri("http://localhost:8086"))

                .build();
    }
}

