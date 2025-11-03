/**
 * @file: TemplateRenderService.java
 * @description: Service interface for template rendering
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template.service;

import java.util.Map;

public interface TemplateRenderService {

    /**
     * Render template with organization data
     *
     * @param templateId ID of the template
     * @param organizationId ID of the organization
     * @return rendered HTML
     */
    String renderTemplate(Long templateId, Long organizationId);

    /**
     * Render template with custom data
     *
     * @param templateId ID of the template
     * @param data custom data map
     * @return rendered HTML
     */
    String renderTemplateWithData(Long templateId, Map<String, Object> data);
    
    /**
     * Render template with organization and landing data
     *
     * @param templateId ID of the template
     * @param organizationId ID of the organization
     * @param landingId ID of the landing (optional)
     * @return rendered HTML
     */
    String renderTemplateWithOrganizationAndLanding(Long templateId, Long organizationId, Long landingId);

    /**
     * Compile and cache template
     *
     * @param templateId ID of the template
     */
    void compileTemplate(Long templateId);

    /**
     * Clear template cache
     *
     * @param templateId ID of the template
     */
    void clearTemplateCache(Long templateId);
}

