/**
 * @file: GoogleSheetsService.java
 * @description: Service interface for Google Sheets integration
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.integration.service;

import com.baganov.klassifikator.integration.model.dto.GoogleSheetsSyncDto;

import java.util.List;
import java.util.Map;

public interface GoogleSheetsService {

    GoogleSheetsSyncDto createSync(GoogleSheetsSyncDto dto);

    GoogleSheetsSyncDto getSyncByOrganization(Long organizationId);

    void syncOrganizationData(Long organizationId);

    void syncAllActiveOrganizations();

    List<Map<String, Object>> readSpreadsheetData(String spreadsheetId, String range);
}

