/**
 * @file: LandingResponseDto.java
 * @description: DTO for landing response
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingResponseDto {

    private Long id;
    private Long organizationId;
    private String domain;
    private String subdomain;
    private Long templateId;
    private String status;
    private Boolean sslEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}

