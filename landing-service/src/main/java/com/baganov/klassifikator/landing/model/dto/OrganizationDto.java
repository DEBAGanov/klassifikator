/**
 * @file: OrganizationDto.java
 * @description: DTO for organization
 * @dependencies: Jakarta Validation
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String category;
    private String type;
    private String address;
    private String phone;
    private String website;
    private String workingHours;
    private String status;
    private String googleSheetId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

