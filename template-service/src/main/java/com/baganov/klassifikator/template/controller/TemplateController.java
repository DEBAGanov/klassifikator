/**
 * @file: TemplateController.java
 * @description: REST controller for Template operations
 * @dependencies: Spring Web
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template.controller;

import com.baganov.klassifikator.template.model.dto.TemplateDto;
import com.baganov.klassifikator.template.service.TemplateService;
import com.baganov.klassifikator.template.service.TemplateRenderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateRenderService templateRenderService;

    @PostMapping
    public ResponseEntity<TemplateDto> createTemplate(@Valid @RequestBody TemplateDto dto) {
        log.info("POST /api/v1/templates - Creating template");
        TemplateDto response = templateService.createTemplate(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> getTemplateById(@PathVariable Long id) {
        log.info("GET /api/v1/templates/{}", id);
        TemplateDto response = templateService.getTemplateById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TemplateDto>> getAllTemplates(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        log.info("GET /api/v1/templates?activeOnly={}", activeOnly);
        List<TemplateDto> response = activeOnly
                ? templateService.getActiveTemplates()
                : templateService.getAllTemplates();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateDto> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateDto dto) {
        log.info("PUT /api/v1/templates/{}", id);
        TemplateDto response = templateService.updateTemplate(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        log.info("DELETE /api/v1/templates/{}", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{templateId}/render")
    public ResponseEntity<String> renderTemplate(
            @PathVariable Long templateId,
            @RequestParam Long organizationId,
            @RequestParam(required = false) Long landingId) {
        log.info("GET /api/v1/templates/{}/render?organizationId={}&landingId={}", templateId, organizationId, landingId);
        
        // Create data map with organizationId and landingId
        Map<String, Object> data = new HashMap<>();
        data.put("organizationId", organizationId);
        if (landingId != null) {
            data.put("landingId", landingId);
        }
        
        String html = templateRenderService.renderTemplateWithOrganizationAndLanding(templateId, organizationId, landingId);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                // Disable browser caching for development
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(html);
    }

    @PostMapping("/{templateId}/render-with-data")
    public ResponseEntity<String> renderTemplateWithData(
            @PathVariable Long templateId,
            @RequestBody Map<String, Object> data) {
        log.info("POST /api/v1/templates/{}/render-with-data", templateId);
        String html = templateRenderService.renderTemplateWithData(templateId, data);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @PostMapping("/{templateId}/compile")
    public ResponseEntity<Void> compileTemplate(@PathVariable Long templateId) {
        log.info("POST /api/v1/templates/{}/compile", templateId);
        templateRenderService.compileTemplate(templateId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{templateId}/cache")
    public ResponseEntity<Void> clearTemplateCache(@PathVariable Long templateId) {
        log.info("DELETE /api/v1/templates/{}/cache", templateId);
        templateRenderService.clearTemplateCache(templateId);
        return ResponseEntity.noContent().build();
    }
}

