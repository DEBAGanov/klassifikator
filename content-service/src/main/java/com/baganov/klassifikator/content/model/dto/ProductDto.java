/**
 * @file: ProductDto.java
 * @description: DTO for product
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    private String category;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;
    private BigDecimal price;
    private Long imageId;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

