/**
 * @file: TemplateDto.java
 * @description: DTO for template
 * @dependencies: Jakarta Validation
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDto {

    private Long id;

    @NotBlank(message = "Template name is required")
    private String name;

    private String description;
    private String version;
    private String htmlStructure;
    private String cssStyles;
    private String jsScripts;
    private Map<String, Object> config;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

