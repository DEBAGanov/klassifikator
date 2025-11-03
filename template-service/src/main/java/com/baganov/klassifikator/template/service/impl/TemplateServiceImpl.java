/**
 * @file: TemplateServiceImpl.java
 * @description: Implementation of TemplateService
 * @dependencies: Spring, Lombok
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template.service.impl;

import com.baganov.klassifikator.common.model.entity.Template;
import com.baganov.klassifikator.template.mapper.TemplateMapper;
import com.baganov.klassifikator.template.model.dto.TemplateDto;
import com.baganov.klassifikator.template.repository.TemplateRepository;
import com.baganov.klassifikator.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;

    @Override
    @Transactional
    @CacheEvict(value = "template", allEntries = true)
    public TemplateDto createTemplate(TemplateDto dto) {
        log.info("Creating template: {}", dto.getName());

        Template template = templateMapper.toEntity(dto);
        Template saved = templateRepository.save(template);

        log.info("Successfully created template with id {}", saved.getId());
        return templateMapper.toDto(saved);
    }

    @Override
    @Cacheable(value = "template", key = "#id")
    public TemplateDto getTemplateById(Long id) {
        log.debug("Fetching template with id {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        return templateMapper.toDto(template);
    }

    @Override
    public List<TemplateDto> getAllTemplates() {
        log.debug("Fetching all templates");

        List<Template> templates = templateRepository.findAll();
        return templateMapper.toDtoList(templates);
    }

    @Override
    @Cacheable(value = "template", key = "'active'")
    public List<TemplateDto> getActiveTemplates() {
        log.debug("Fetching active templates");

        List<Template> templates = templateRepository.findByIsActive(true);
        return templateMapper.toDtoList(templates);
    }

    @Override
    @Transactional
    @CacheEvict(value = "template", allEntries = true)
    public TemplateDto updateTemplate(Long id, TemplateDto dto) {
        log.info("Updating template with id {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        templateMapper.updateEntity(dto, template);
        Template updated = templateRepository.save(template);

        log.info("Successfully updated template with id {}", id);
        return templateMapper.toDto(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "template", allEntries = true)
    public void deleteTemplate(Long id) {
        log.info("Deleting template with id {}", id);

        if (!templateRepository.existsById(id)) {
            throw new RuntimeException("Template not found with id: " + id);
        }

        templateRepository.deleteById(id);
        log.info("Successfully deleted template with id {}", id);
    }

    @Override
    public String renderTemplate(Long templateId, Long organizationId) {
        log.info("Rendering template {} for organization {}", templateId, organizationId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

        // Return raw HTML structure (actual rendering is done by TemplateRenderService)
        return template.getHtmlStructure();
    }
}

