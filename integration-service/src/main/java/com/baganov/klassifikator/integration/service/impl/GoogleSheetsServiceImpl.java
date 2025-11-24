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

    @Override
    public Map<String, Object> syncAllOrganizationsFromSheet(String spreadsheetId, String sheetName) {
        log.info("Syncing all organizations from spreadsheet: {}, sheet: {}", spreadsheetId, sheetName);
        
        // Use default spreadsheet ID from first sync record if not provided
        if (spreadsheetId == null || spreadsheetId.isEmpty()) {
            GoogleSheetsSync firstSync = syncRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No sync configuration found and spreadsheetId not provided"));
            spreadsheetId = firstSync.getSpreadsheetId();
        }
        
        int created = 0;
        int updated = 0;
        int failed = 0;
        int deleted = 0;
        int total = 0;
        List<String> errors = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        Set<String> sheetDomains = new HashSet<>();  // Track domains from Google Sheets
        
        try {
            // Read all rows from Google Sheets
            rows = readSpreadsheetData(spreadsheetId, sheetName + "!A:AB");
            total = rows.size();
            log.info("Read {} rows from Google Sheets (Organizations)", total);
            
            // Read products from separate sheet
            List<Map<String, Object>> productsRows = new ArrayList<>();
            try {
                productsRows = readSpreadsheetData(spreadsheetId, "goods!A:F");
                log.info("Read {} rows from Google Sheets (goods/products)", productsRows.size());
            } catch (Exception e) {
                log.warn("Failed to read products sheet 'goods': {}", e.getMessage());
            }
            
            // Read promotions from separate sheet
            List<Map<String, Object>> promotionsRows = new ArrayList<>();
            // Try different sheet name variants
            String[] promotionSheetNames = {"promotion", "Promotions", "Акции", "акции"};
            for (String sheetNameVariant : promotionSheetNames) {
                try {
                    promotionsRows = readSpreadsheetData(spreadsheetId, sheetNameVariant + "!A:D");
                    log.info("Read {} rows from Google Sheets (promotions/{})", promotionsRows.size(), sheetNameVariant);
                    break; // Success, stop trying
                } catch (Exception e) {
                    log.debug("Failed to read promotions sheet '{}': {}", sheetNameVariant, e.getMessage());
                }
            }
            if (promotionsRows.isEmpty()) {
                log.warn("Could not read promotions from any sheet variant");
            }
            
            // Group products by domain
            Map<String, List<Map<String, Object>>> productsByDomain = groupByDomain(productsRows);
            log.info("Grouped products into {} domains", productsByDomain.size());
            
            // Group promotions by domain
            Map<String, List<Map<String, Object>>> promotionsByDomain = groupByDomain(promotionsRows);
            log.info("Grouped promotions into {} domains", promotionsByDomain.size());
            
            for (Map<String, Object> row : rows) {
                try {
                    String domain = getStringValue(row, "Домен", "").trim();
                    String name = getStringValue(row, "Название", "").trim();
                    
                    // Skip empty rows
                    if (domain.isEmpty() || name.isEmpty()) {
                        log.debug("Skipping row with empty domain or name");
                        continue;
                    }
                    
                    // Clean domain: remove protocol and trailing slashes
                    domain = domain.replaceAll("^https?://", "").replaceAll("/$", "");
                    
                    // Track this domain
                    sheetDomains.add(domain);
                    
                    // Get products and promotions for this domain
                    List<Map<String, Object>> domainProducts = productsByDomain.getOrDefault(domain, new ArrayList<>());
                    List<Map<String, Object>> domainPromotions = promotionsByDomain.getOrDefault(domain, new ArrayList<>());
                    
                    log.info("Domain {} has {} products and {} promotions", domain, domainProducts.size(), domainPromotions.size());
                    
                    // Process this organization with its products and promotions
                    boolean isNew = dataProcessor.processOrganizationRow(row, domain, name, domainProducts, domainPromotions);
                    
                    if (isNew) {
                        created++;
                        log.info("✅ Created new organization: {} ({}) with {} products, {} promotions", 
                                name, domain, domainProducts.size(), domainPromotions.size());
                    } else {
                        updated++;
                        log.info("🔄 Updated existing organization: {} ({}) with {} products, {} promotions", 
                                name, domain, domainProducts.size(), domainPromotions.size());
                    }
                    
                } catch (Exception e) {
                    failed++;
                    String errorMsg = "Failed to process row: " + e.getMessage();
                    errors.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }
            
            // Delete organizations/landings that are NOT in Google Sheets
            try {
                deleted = dataProcessor.deleteOrganizationsNotInSheet(sheetDomains);
                log.info("🗑️  Deleted {} organizations not in Google Sheets", deleted);
            } catch (Exception e) {
                log.error("Failed to delete old organizations", e);
                errors.add("Failed to delete old organizations: " + e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Failed to sync all organizations", e);
            throw new RuntimeException("Failed to sync all organizations: " + e.getMessage());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("created", created);
        result.put("deleted", deleted);
        result.put("updated", updated);
        result.put("failed", failed);
        result.put("errors", errors);
        result.put("status", "SUCCESS");
        result.put("timestamp", LocalDateTime.now());
        
        log.info("Sync completed: {} created, {} updated, {} failed", created, updated, failed);
        return result;
    }
    
    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null && !value.toString().trim().isEmpty() ? value.toString().trim() : defaultValue;
    }
    
    /**
     * Group rows by domain
     * @param rows List of rows from Google Sheets
     * @return Map of domain to list of rows
     */
    private Map<String, List<Map<String, Object>>> groupByDomain(List<Map<String, Object>> rows) {
        Map<String, List<Map<String, Object>>> grouped = new HashMap<>();
        
        for (Map<String, Object> row : rows) {
            String domain = getStringValue(row, "Домен", "").trim();
            
            // Skip empty domains
            if (domain.isEmpty()) {
                continue;
            }
            
            // Clean domain: remove protocol and trailing slashes
            domain = domain.replaceAll("^https?://", "").replaceAll("/$", "");
            
            // Add row to domain group
            grouped.computeIfAbsent(domain, k -> new ArrayList<>()).add(row);
        }
        
        return grouped;
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
    
    @Override
    public List<String> getSheetNames(String spreadsheetId) {
        try {
            log.info("Getting sheet names for spreadsheet: {}", spreadsheetId);
            com.google.api.services.sheets.v4.model.Spreadsheet spreadsheet = 
                sheetsService.spreadsheets()
                    .get(spreadsheetId)
                    .execute();
            
            return spreadsheet.getSheets().stream()
                .map(sheet -> sheet.getProperties().getTitle())
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get sheet names: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get sheet names: " + e.getMessage(), e);
        }
    }
}

