/**
 * @file: PromotionDto.java
 * @description: DTO for promotion
 * @dependencies: Jakarta Validation
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDto {

    private Long id;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private Long imageId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

