/**
 * @file: LandingController.java
 * @description: Controller for rendering landing pages by subdomain
 * @dependencies: Spring Web, Template Render Service
 * @created: 2025-11-19
 */
package com.baganov.klassifikator.template.controller;

import com.baganov.klassifikator.template.service.TemplateRenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controller for serving landing pages to end users
 * Handles requests like: https://modernissimo.volzhck.ru
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LandingController {

    private final TemplateRenderService templateRenderService;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.landing-service.url:http://localhost:8081}")
    private String landingServiceUrl;

    @Value("${landing.base-domain:volzhck.ru}")
    private String baseDomain;

    /**
     * Main endpoint for rendering landing pages
     * Accepts requests to any subdomain like modernissimo.volzhck.ru
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> renderLanding(@RequestHeader(value = "Host", required = false) String host) {
        log.info("Rendering landing for host: {}", host);

        try {
            // Extract subdomain from host
            String subdomain = extractSubdomain(host);
            if (subdomain == null || subdomain.isEmpty()) {
                log.warn("Could not extract subdomain from host: {}", host);
                return ResponseEntity.notFound().build();
            }

            // Fetch landing data from Landing Service
            Map<String, Object> landingData = fetchLandingBySubdomain(subdomain);
            if (landingData == null) {
                log.warn("Landing not found for subdomain: {}", subdomain);
                return ResponseEntity.notFound().build();
            }

            // Check if landing is active
            String status = (String) landingData.get("status");
            if (!"ACTIVE".equals(status) && !"PUBLISHED".equals(status)) {
                log.warn("Landing {} is not active: {}", subdomain, status);
                return ResponseEntity.status(503)
                        .body("<html><body><h1>Сайт временно недоступен</h1><p>Пожалуйста, попробуйте позже.</p></body></html>");
            }

            // Extract IDs
            Long templateId = getLongValue(landingData.get("templateId"));
            Long organizationId = getLongValue(landingData.get("organizationId"));
            Long landingId = getLongValue(landingData.get("id"));

            if (templateId == null || organizationId == null) {
                log.error("Missing templateId or organizationId for landing: {}", subdomain);
                return ResponseEntity.status(500)
                        .body("<html><body><h1>Ошибка конфигурации</h1></body></html>");
            }

            // Render template
            String html = templateRenderService.renderTemplateWithOrganizationAndLanding(
                    templateId, organizationId, landingId);

            log.info("Successfully rendered landing: {}", subdomain);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header("Cache-Control", "public, max-age=300") // 5 min cache
                    .body(html);

        } catch (Exception e) {
            log.error("Failed to render landing for host: {}", host, e);
            return ResponseEntity.status(500)
                    .body("<html><body><h1>Произошла ошибка</h1><p>Пожалуйста, обновите страницу.</p></body></html>");
        }
    }

    /**
     * Health check endpoint for root domain
     */
    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    /**
     * Extract subdomain from host header
     * Example: modernissimo.volzhck.ru -> modernissimo
     */
    private String extractSubdomain(String host) {
        if (host == null || host.isEmpty()) {
            return null;
        }

        // Remove port if present
        host = host.split(":")[0];

        // Remove base domain
        if (host.endsWith("." + baseDomain)) {
            return host.substring(0, host.length() - baseDomain.length() - 1);
        }

        // If host is exactly the base domain, return null (no subdomain)
        if (host.equals(baseDomain)) {
            return null;
        }

        return host;
    }

    /**
     * Fetch landing data from Landing Service by subdomain
     */
    private Map<String, Object> fetchLandingBySubdomain(String subdomain) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(landingServiceUrl).build();

            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/landings/by-subdomain/{subdomain}")
                            .build(subdomain))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> {
                        log.error("Failed to fetch landing for subdomain: {}", subdomain, e);
                        return Mono.empty();
                    })
                    .block();

        } catch (Exception e) {
            log.error("Exception while fetching landing for subdomain: {}", subdomain, e);
            return null;
        }
    }

    /**
     * Convert Object to Long safely
     */
    private Long getLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

