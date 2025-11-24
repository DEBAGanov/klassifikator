/**
 * @file: GatewayConfig.java
 * @description: Configuration for API Gateway routes
 * @dependencies: Spring Cloud Gateway
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${services.landing.url:http://localhost:8081}")
    private String landingServiceUrl;
    
    @Value("${services.content.url:http://localhost:8082}")
    private String contentServiceUrl;
    
    @Value("${services.template.url:http://localhost:8083}")
    private String templateServiceUrl;
    
    @Value("${services.media.url:http://localhost:8084}")
    private String mediaServiceUrl;
    
    @Value("${services.integration.url:http://localhost:8085}")
    private String integrationServiceUrl;
    
    @Value("${services.order.url:http://localhost:8086}")
    private String orderServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Landing Service routes
                .route("landing-service", r -> r
                        .path("/api/v1/landings/**", "/api/v1/organizations/**")
                        .uri(landingServiceUrl))

                // Content Service routes
                .route("content-service", r -> r
                        .path("/api/v1/content/**", "/api/v1/products/**", "/api/v1/promotions/**")
                        .uri(contentServiceUrl))

                // Template Service routes
                .route("template-service", r -> r
                        .path("/api/v1/templates/**")
                        .uri(templateServiceUrl))

                // Media Service routes
                .route("media-service", r -> r
                        .path("/api/v1/media/**")
                        .uri(mediaServiceUrl))

                // Integration Service routes
                .route("integration-service", r -> r
                        .path("/api/v1/integration/**")
                        .uri(integrationServiceUrl))

                // Order Service routes
                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .uri(orderServiceUrl))

                .build();
    }
}

