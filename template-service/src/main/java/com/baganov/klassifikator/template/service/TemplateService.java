/**
 * @file: TemplateService.java
 * @description: Service interface for Template operations
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template.service;

import com.baganov.klassifikator.template.model.dto.TemplateDto;

import java.util.List;

public interface TemplateService {

    TemplateDto createTemplate(TemplateDto dto);

    TemplateDto getTemplateById(Long id);

    List<TemplateDto> getAllTemplates();

    List<TemplateDto> getActiveTemplates();

    TemplateDto updateTemplate(Long id, TemplateDto dto);

    void deleteTemplate(Long id);

    String renderTemplate(Long templateId, Long organizationId);
}

