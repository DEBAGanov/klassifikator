/**
 * @file: GoogleSheetsSyncDto.java
 * @description: DTO for Google Sheets sync configuration
 * @dependencies: Jakarta Validation
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.integration.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleSheetsSyncDto {

    private Long id;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotBlank(message = "Spreadsheet ID is required")
    private String spreadsheetId;

    private String sheetName;
    private Integer syncIntervalMinutes;
    private Boolean isActive;
    private LocalDateTime lastSyncAt;
    private String lastSyncStatus;
    private LocalDateTime createdAt;
}

