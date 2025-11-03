/**
 * @file: IntegrationController.java
 * @description: REST controller for Integration operations
 * @dependencies: Spring Web
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.integration.controller;

import com.baganov.klassifikator.integration.model.dto.GoogleSheetsSyncDto;
import com.baganov.klassifikator.integration.model.dto.TelegramNotificationDto;
import com.baganov.klassifikator.integration.service.GoogleSheetsService;
import com.baganov.klassifikator.integration.service.TelegramService;
import com.baganov.klassifikator.integration.service.TelegramNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/integration")
@RequiredArgsConstructor
public class IntegrationController {

    private final GoogleSheetsService googleSheetsService;
    private final TelegramService telegramService;
    private final TelegramNotificationService telegramNotificationService;

    // Google Sheets endpoints
    @PostMapping("/google-sheets/sync")
    public ResponseEntity<GoogleSheetsSyncDto> createSync(@Valid @RequestBody GoogleSheetsSyncDto dto) {
        log.info("POST /api/v1/integration/google-sheets/sync - Creating sync");
        GoogleSheetsSyncDto response = googleSheetsService.createSync(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/google-sheets/sync/organization/{organizationId}")
    public ResponseEntity<GoogleSheetsSyncDto> getSyncByOrganization(@PathVariable Long organizationId) {
        log.info("GET /api/v1/integration/google-sheets/sync/organization/{}", organizationId);
        GoogleSheetsSyncDto response = googleSheetsService.getSyncByOrganization(organizationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google-sheets/sync/organization/{organizationId}/trigger")
    public ResponseEntity<Void> triggerSync(@PathVariable Long organizationId) {
        log.info("POST /api/v1/integration/google-sheets/sync/organization/{}/trigger", organizationId);
        googleSheetsService.syncOrganizationData(organizationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/google-sheets/read")
    public ResponseEntity<List<Map<String, Object>>> readSpreadsheet(
            @RequestParam String spreadsheetId,
            @RequestParam(defaultValue = "A:Z") String range) {
        log.info("GET /api/v1/integration/google-sheets/read?spreadsheetId={}&range={}", spreadsheetId, range);
        List<Map<String, Object>> data = googleSheetsService.readSpreadsheetData(spreadsheetId, range);
        return ResponseEntity.ok(data);
    }

    // Telegram endpoints
    @PostMapping("/telegram/send")
    public ResponseEntity<Void> sendTelegramNotification(@Valid @RequestBody TelegramNotificationDto dto) {
        log.info("POST /api/v1/integration/telegram/send");
        telegramService.sendNotification(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/telegram/order/{orderId}/notify")
    public ResponseEntity<Void> notifyOrder(
            @PathVariable Long orderId,
            @RequestParam Long organizationId) {
        log.info("POST /api/v1/integration/telegram/order/{}/notify?organizationId={}", orderId, organizationId);
        telegramService.sendOrderNotification(orderId, organizationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/telegram/notify/order")
    public ResponseEntity<Void> sendOrderNotification(@RequestBody Map<String, Object> orderData) {
        log.info("POST /api/v1/integration/telegram/notify/order - Sending order notification");
        telegramNotificationService.sendOrderNotification(orderData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/telegram/test")
    public ResponseEntity<Map<String, Object>> testTelegramNotification() {
        log.info("POST /api/v1/integration/telegram/test - Testing Telegram notifications");
        boolean success = telegramNotificationService.sendTestMessage();
        Map<String, Object> response = Map.of(
            "success", success,
            "message", success ? "Test message sent successfully" : "Failed to send test message"
        );
        return ResponseEntity.ok(response);
    }
}

