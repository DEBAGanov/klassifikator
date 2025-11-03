/**
 * @file: GoogleSheetsServiceImpl.java
 * @description: Implementation of GoogleSheetsService
 * @dependencies: Spring, Google Sheets API, Lombok
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.integration.service.impl;

import com.baganov.klassifikator.common.model.entity.GoogleSheetsSync;
import com.baganov.klassifikator.integration.model.dto.GoogleSheetsSyncDto;
import com.baganov.klassifikator.integration.repository.GoogleSheetsSyncRepository;
import com.baganov.klassifikator.integration.service.GoogleSheetsService;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private final GoogleSheetsSyncRepository syncRepository;
    private final Sheets sheetsService;
    private final com.baganov.klassifikator.integration.service.GoogleSheetsDataProcessor dataProcessor;

    @Override
    @Transactional
    public GoogleSheetsSyncDto createSync(GoogleSheetsSyncDto dto) {
        log.info("Creating Google Sheets sync for organization {}", dto.getOrganizationId());

        GoogleSheetsSync sync = new GoogleSheetsSync();
        sync.setOrganizationId(dto.getOrganizationId());
        sync.setSpreadsheetId(dto.getSpreadsheetId());
        sync.setSheetName(dto.getSheetName());
        sync.setSyncIntervalMinutes(dto.getSyncIntervalMinutes() != null ? dto.getSyncIntervalMinutes() : 30);
        sync.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        GoogleSheetsSync saved = syncRepository.save(sync);

        log.info("Successfully created sync with id {}", saved.getId());
        return mapToDto(saved);
    }

    @Override
    public GoogleSheetsSyncDto getSyncByOrganization(Long organizationId) {
        log.debug("Fetching sync for organization {}", organizationId);

        GoogleSheetsSync sync = syncRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new RuntimeException("Sync not found for organization: " + organizationId));

        return mapToDto(sync);
    }

    @Override
    @Transactional
    public void syncOrganizationData(Long organizationId) {
        log.info("Syncing data for organization {}", organizationId);

        GoogleSheetsSync sync = syncRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new RuntimeException("Sync not found for organization: " + organizationId));

        try {
            String range = sync.getSheetName() != null ? sync.getSheetName() + "!A:Z" : "A:Z";
            List<Map<String, Object>> data = readSpreadsheetData(sync.getSpreadsheetId(), range);

            log.info("Read {} rows from spreadsheet", data.size());

            // Process data and update organization
            dataProcessor.processAndUpdateOrganization(organizationId, data);

            sync.setLastSyncAt(LocalDateTime.now());
            sync.setLastSyncStatus("SUCCESS - Processed " + data.size() + " rows");
            syncRepository.save(sync);

            log.info("Successfully synced organization {}", organizationId);

        } catch (Exception e) {
            log.error("Failed to sync organization {}", organizationId, e);
            sync.setLastSyncAt(LocalDateTime.now());
            sync.setLastSyncStatus("FAILED: " + e.getMessage());
            syncRepository.save(sync);
            throw new RuntimeException("Sync failed: " + e.getMessage());
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${google.sheets.sync-interval-ms:1800000}") // 30 minutes
    public void syncAllActiveOrganizations() {
        log.info("Starting scheduled sync for all active organizations");

        List<GoogleSheetsSync> activeSyncs = syncRepository.findByIsActiveTrue();
        log.info("Found {} active syncs", activeSyncs.size());

        for (GoogleSheetsSync sync : activeSyncs) {
            try {
                syncOrganizationData(sync.getOrganizationId());
            } catch (Exception e) {
                log.error("Failed to sync organization {}", sync.getOrganizationId(), e);
            }
        }

        log.info("Completed scheduled sync");
    }

    @Override
    public List<Map<String, Object>> readSpreadsheetData(String spreadsheetId, String range) {
        log.debug("Reading data from spreadsheet {} range {}", spreadsheetId, range);

        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                log.warn("No data found in spreadsheet");
                return Collections.emptyList();
            }

            // First row is headers
            List<Object> headers = values.get(0);
            List<Map<String, Object>> result = new ArrayList<>();

            for (int i = 1; i < values.size(); i++) {
                List<Object> row = values.get(i);
                Map<String, Object> rowData = new HashMap<>();

                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j).toString();
                    Object value = j < row.size() ? row.get(j) : "";
                    rowData.put(header, value);
                }

                result.add(rowData);
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to read spreadsheet data", e);
            throw new RuntimeException("Failed to read spreadsheet: " + e.getMessage());
        }
    }

    private GoogleSheetsSyncDto mapToDto(GoogleSheetsSync entity) {
        return GoogleSheetsSyncDto.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .spreadsheetId(entity.getSpreadsheetId())
                .sheetName(entity.getSheetName())
                .syncIntervalMinutes(entity.getSyncIntervalMinutes())
                .isActive(entity.getIsActive())
                .lastSyncAt(entity.getLastSyncAt())
                .lastSyncStatus(entity.getLastSyncStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

