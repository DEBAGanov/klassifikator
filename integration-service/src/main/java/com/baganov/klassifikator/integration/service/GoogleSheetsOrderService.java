/**
 * @file: GoogleSheetsOrderService.java
 * @description: Service for writing orders to Google Sheets
 * @dependencies: Google Sheets API
 * @created: 2025-11-22
 */
package com.baganov.klassifikator.integration.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetsOrderService {

    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private static final String ORDERS_SHEET_NAME = "orders";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Write order to Google Sheets
     *
     * @param orderData Map containing order information:
     *                  - orderId: Order ID
     *                  - orderNumber: Order number
     *                  - domain: Domain of the landing
     *                  - totalAmount: Total order amount
     *                  - customerName: Customer name
     *                  - customerEmail: Customer email
     *                  - customerPhone: Customer phone
     *                  - deliveryAddress: Delivery address
     *                  - comment: Order comment
     *                  - items: List of order items
     *                  - organizationTelegramBot: Organization's custom Telegram bot (optional)
     */
    public void writeOrderToSheet(Map<String, Object> orderData) {
        try {
            log.info("Writing order #{} to Google Sheets", orderData.get("orderId"));

            // Format order data for Google Sheets
            List<Object> row = formatOrderRow(orderData);

            // Prepare value range
            ValueRange body = new ValueRange()
                    .setValues(Collections.singletonList(row));

            // Append to sheet
            String range = ORDERS_SHEET_NAME + "!A:L";  // Columns A through L
            AppendValuesResponse result = sheetsService.spreadsheets().values()
                    .append(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();

            log.info("✅ Order #{} successfully written to Google Sheets. Updated {} rows.",
                    orderData.get("orderId"),
                    result.getUpdates().getUpdatedRows());

        } catch (Exception e) {
            log.error("❌ Failed to write order #{} to Google Sheets: {}",
                    orderData.get("orderId"), e.getMessage(), e);
            // Don't throw exception - order creation should not fail if Google Sheets write fails
        }
    }

    /**
     * Format order data into a row for Google Sheets
     * Columns: Дата | Домен | Заказ № | Сумма | Товары | Имя | email | Телефон | Адрес доставки | Комментарий к заказу | Бот клиента
     */
    private List<Object> formatOrderRow(Map<String, Object> orderData) {
        List<Object> row = new ArrayList<>();

        // 1. Дата (Date)
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        row.add(date);

        // 2. Домен (Domain)
        String domain = (String) orderData.getOrDefault("domain", "");
        row.add(domain);

        // 3. Заказ № (Order Number)
        Object orderNumber = orderData.get("orderId");
        row.add(orderNumber != null ? orderNumber.toString() : "");

        // 4. Сумма (Total Amount)
        Object totalAmount = orderData.get("totalAmount");
        row.add(totalAmount != null ? totalAmount.toString() : "0");

        // 5. Товары (Products) - formatted as "Product1 x2, Product2 x1"
        String productsStr = formatProductsList(orderData.get("items"));
        row.add(productsStr);

        // 6. Имя (Customer Name)
        String customerName = (String) orderData.getOrDefault("customerName", "");
        row.add(customerName);

        // 7. email (Customer Email)
        String customerEmail = (String) orderData.getOrDefault("customerEmail", "");
        row.add(customerEmail);

        // 8. Телефон (Customer Phone)
        String customerPhone = (String) orderData.getOrDefault("customerPhone", "");
        row.add(customerPhone);

        // 9. Адрес доставки (Delivery Address)
        String deliveryAddress = (String) orderData.getOrDefault("deliveryAddress", "");
        row.add(deliveryAddress);

        // 10. Комментарий к заказу (Order Comment)
        String comment = (String) orderData.getOrDefault("comment", "");
        row.add(comment);

        // 11. Бот клиента (Organization's custom Telegram bot)
        String organizationTelegramBot = (String) orderData.getOrDefault("organizationTelegramBot", "");
        row.add(organizationTelegramBot);

        return row;
    }

    /**
     * Format products list into a string
     * Example: "Стол офисный x2, Кресло x1"
     */
    @SuppressWarnings("unchecked")
    private String formatProductsList(Object itemsObj) {
        if (itemsObj == null) {
            return "";
        }

        try {
            List<Map<String, Object>> items = (List<Map<String, Object>>) itemsObj;
            
            return items.stream()
                    .map(item -> {
                        String productName = (String) item.getOrDefault("productName", "Unknown");
                        Object quantity = item.get("quantity");
                        return String.format("%s x%s", productName, quantity != null ? quantity.toString() : "1");
                    })
                    .collect(Collectors.joining(", "));
                    
        } catch (ClassCastException e) {
            log.warn("Failed to parse items list: {}", e.getMessage());
            return "";
        }
    }
}

