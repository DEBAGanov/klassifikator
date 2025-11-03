/**
 * @file: TemplateRenderServiceImpl.java
 * @description: Implementation of TemplateRenderService with Handlebars
 * @dependencies: Handlebars, WebClient
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template.service.impl;

import com.baganov.klassifikator.common.model.entity.Template;
import com.baganov.klassifikator.template.repository.TemplateRepository;
import com.baganov.klassifikator.template.service.TemplateRenderService;
import com.github.jknack.handlebars.Handlebars;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateRenderServiceImpl implements TemplateRenderService {

    private final TemplateRepository templateRepository;
    private final Handlebars handlebars;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.content-service.url:http://localhost:8082}")
    private String contentServiceUrl;

    @Value("${services.landing-service.url:http://localhost:8081}")
    private String landingServiceUrl;

    // Cache for compiled templates
    private final Map<Long, com.github.jknack.handlebars.Template> compiledTemplates = new ConcurrentHashMap<>();

    @Override
    @Cacheable(value = "renderedTemplate", key = "#templateId + '_' + #organizationId")
    public String renderTemplate(Long templateId, Long organizationId) {
        log.info("Rendering template {} for organization {}", templateId, organizationId);

        try {
            // Get template
            Template template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

            // Fetch organization data from Content Service
            Map<String, Object> data = fetchOrganizationData(organizationId);
            
            // Add organizationId and landingId for JavaScript configuration
            data.put("organizationId", organizationId);
            data.putIfAbsent("landingId", "");  // Will be set if provided in query params

            // Compile template if not cached
            com.github.jknack.handlebars.Template compiledTemplate = getCompiledTemplate(templateId, template);

            // Render
            String rendered = compiledTemplate.apply(data);
            
            // Inject CSS and JS into HTML
            rendered = injectStylesAndScripts(rendered, template);

            log.info("Successfully rendered template {} for organization {}", templateId, organizationId);
            return rendered;

        } catch (Exception e) {
            log.error("Failed to render template {} for organization {}", templateId, organizationId, e);
            throw new RuntimeException("Failed to render template: " + e.getMessage(), e);
        }
    }

    @Override
    public String renderTemplateWithData(Long templateId, Map<String, Object> data) {
        log.info("Rendering template {} with custom data", templateId);

        try {
            // Get template
            Template template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

            // Compile template if not cached
            com.github.jknack.handlebars.Template compiledTemplate = getCompiledTemplate(templateId, template);

            // Render
            String rendered = compiledTemplate.apply(data);
            
            // Inject CSS and JS into HTML
            rendered = injectStylesAndScripts(rendered, template);

            log.info("Successfully rendered template {} with custom data", templateId);
            return rendered;

        } catch (Exception e) {
            log.error("Failed to render template {} with custom data", templateId, e);
            throw new RuntimeException("Failed to render template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Render template with organizationId and landingId
     */
    public String renderTemplateWithOrganizationAndLanding(Long templateId, Long organizationId, Long landingId) {
        log.info("Rendering template {} for organization {} and landing {}", templateId, organizationId, landingId);

        try {
            // Get template
            Template template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

            // Fetch organization data from Content Service
            Map<String, Object> data = fetchOrganizationData(organizationId);
            
            // Add organizationId and landingId for JavaScript configuration
            data.put("organizationId", organizationId);
            data.put("landingId", landingId != null ? landingId : "");

            // Compile template if not cached
            com.github.jknack.handlebars.Template compiledTemplate = getCompiledTemplate(templateId, template);

            // Render
            String rendered = compiledTemplate.apply(data);
            
            // Inject CSS and JS into HTML
            rendered = injectStylesAndScripts(rendered, template);

            log.info("Successfully rendered template {} for organization {} and landing {}", templateId, organizationId, landingId);
            return rendered;

        } catch (Exception e) {
            log.error("Failed to render template {} for organization {}", templateId, organizationId, e);
            throw new RuntimeException("Failed to render template: " + e.getMessage(), e);
        }
    }

    @Override
    public void compileTemplate(Long templateId) {
        log.info("Compiling template {}", templateId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

        try {
            com.github.jknack.handlebars.Template compiled = handlebars.compileInline(template.getHtmlStructure());
            compiledTemplates.put(templateId, compiled);
            log.info("Successfully compiled template {}", templateId);
        } catch (Exception e) {
            log.error("Failed to compile template {}", templateId, e);
            throw new RuntimeException("Failed to compile template: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = "renderedTemplate", allEntries = true)
    public void clearTemplateCache(Long templateId) {
        log.info("Clearing cache for template {}", templateId);
        compiledTemplates.remove(templateId);
    }

    private com.github.jknack.handlebars.Template getCompiledTemplate(Long templateId, Template template) {
        return compiledTemplates.computeIfAbsent(templateId, id -> {
            try {
                log.debug("Compiling template {} for the first time", templateId);
                return handlebars.compileInline(template.getHtmlStructure());
            } catch (Exception e) {
                throw new RuntimeException("Failed to compile template: " + e.getMessage(), e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchOrganizationData(Long organizationId) {
        log.debug("Fetching data for organization {}", organizationId);

        try {
            WebClient webClient = webClientBuilder.baseUrl(contentServiceUrl).build();

            // Fetch full content from Content Service
            Map<String, Object> fullContent = webClient.get()
                    .uri("/api/v1/content/organization/{id}/full", organizationId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (fullContent == null) {
                log.warn("No content found for organization {}", organizationId);
                return new HashMap<>();
            }

            // Extract and structure data
            Map<String, Object> data = new HashMap<>();

            // Basic content
            Map<String, Object> content = (Map<String, Object>) fullContent.get("content");
            if (content != null && content.get("contentData") != null) {
                Map<String, Object> contentData = (Map<String, Object>) content.get("contentData");
                data.putAll(contentData);
            }

            // Organization info
            if (content != null) {
                data.put("organizationId", content.get("organizationId"));
            }

            // Products
            data.put("products", fullContent.get("products"));

            // Promotions
            data.put("promotions", fullContent.get("promotions"));

            // Add default values if missing
            data.putIfAbsent("title", "Добро пожаловать");
            data.putIfAbsent("description", "");
            data.putIfAbsent("h1", "Наша компания");
            data.putIfAbsent("organizationName", "Организация");

            log.debug("Successfully fetched data for organization {}", organizationId);
            return data;

        } catch (Exception e) {
            log.error("Failed to fetch data for organization {}", organizationId, e);
            // Return empty data instead of failing
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("title", "Ошибка загрузки данных");
            emptyData.put("organizationName", "Организация");
            return emptyData;
        }
    }
    
    /**
     * Inject CSS and JS into rendered HTML
     */
    private String injectStylesAndScripts(String html, Template template) {
        // Replace <link rel="stylesheet" href="styles.css"> with inline styles
        if (template.getCssStyles() != null && !template.getCssStyles().isEmpty()) {
            String inlineStyles = "<style>" + template.getCssStyles() + "</style>";
            html = html.replace("<link rel=\"stylesheet\" href=\"styles.css\">", inlineStyles);
            html = html.replace("<link rel=\"stylesheet\" href=\"order-form.css\">", "");
        }
        
        // Replace <script src="order-form.js"></script> with inline script
        if (template.getJsScripts() != null && !template.getJsScripts().isEmpty()) {
            String inlineScript = "<script>" + template.getJsScripts() + "</script>";
            html = html.replace("<script src=\"order-form.js\"></script>", inlineScript);
        }
        
        return html;
    }
}

