/**
 * @file: TelegramServiceImpl.java
 * @description: Implementation of TelegramService
 * @dependencies: Spring, Telegram Bot API, Lombok
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.integration.service.impl;

import com.baganov.klassifikator.integration.model.dto.TelegramNotificationDto;
import com.baganov.klassifikator.integration.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.api-url:https://api.telegram.org/bot}")
    private String apiUrl;

    @Override
    public void sendNotification(TelegramNotificationDto notification) {
        log.info("Sending Telegram notification to chat {}", notification.getChatId());

        try {
            String url = apiUrl + botToken + "/sendMessage";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", notification.getChatId());
            body.put("text", notification.getMessage());
            if (notification.getParseMode() != null) {
                body.put("parse_mode", notification.getParseMode());
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForObject(url, request, String.class);

            log.info("Successfully sent Telegram notification");

        } catch (Exception e) {
            log.error("Failed to send Telegram notification", e);
            throw new RuntimeException("Failed to send Telegram notification: " + e.getMessage());
        }
    }

    @Override
    public void sendOrderNotification(Long orderId, Long organizationId) {
        log.info("Sending order notification for order {} organization {}", orderId, organizationId);

        // TODO: Fetch order details and organization chat ID from database
        String message = String.format("🛒 Новый заказ #%d\n\nОрганизация: %d\n\nПроверьте детали заказа в панели управления.",
                orderId, organizationId);

        TelegramNotificationDto notification = TelegramNotificationDto.builder()
                .chatId("YOUR_CHAT_ID") // TODO: Get from organization settings
                .message(message)
                .parseMode("HTML")
                .build();

        sendNotification(notification);
    }
}

