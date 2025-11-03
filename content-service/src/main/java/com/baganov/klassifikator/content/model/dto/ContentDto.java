/**
 * @file: ContentDto.java
 * @description: DTO for organization content
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.model.dto;

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
public class ContentDto {

    private Long id;
    private Long organizationId;
    private String title;
    private String metaDescription;
    private String h1;
    private String aboutText;
    private Map<String, Object> contentData;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

