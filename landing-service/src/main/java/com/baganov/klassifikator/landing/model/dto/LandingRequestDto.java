/**
 * @file: LandingRequestDto.java
 * @description: DTO for creating/updating landing
 * @dependencies: Jakarta Validation
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingRequestDto {

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotBlank(message = "Subdomain is required")
    private String subdomain;

    private Long templateId;

    private String status;
}

