/**
 * @file: TelegramNotificationService.java
 * @description: Service for sending notifications via Telegram Bot
 * @dependencies: WebClient
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.integration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final WebClient.Builder webClientBuilder;

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.chat-id:}")
    private String chatId;

    @Value("${telegram.bot.enabled:false}")
    private boolean enabled;

    /**
     * Send order notification to Telegram (both main bot and organization bot if configured)
     */
    public void sendOrderNotification(Map<String, Object> orderData) {
        String message = buildOrderMessage(orderData);
        
        // 1. Send to MAIN bot @ZZZorderbot (ALWAYS)
        if (enabled && !botToken.isEmpty() && !chatId.isEmpty()) {
            try {
                sendMessage(message, botToken, chatId);
                log.info("✅ Telegram notification sent to MAIN bot @ZZZorderbot for order #{}", orderData.get("orderId"));
            } catch (Exception e) {
                log.error("❌ Failed to send Telegram notification to MAIN bot", e);
            }
        } else {
            log.warn("⚠️ Main Telegram bot is not configured. Skipping main bot notification.");
        }
        
        // 2. Send to ORGANIZATION bot (if configured)
        String orgTelegramBotToken = (String) orderData.get("organizationTelegramBotToken");
        String orgTelegramChatId = (String) orderData.get("organizationTelegramChatId");
        
        if (orgTelegramBotToken != null && !orgTelegramBotToken.isEmpty() 
                && orgTelegramChatId != null && !orgTelegramChatId.isEmpty()) {
            try {
                sendMessage(message, orgTelegramBotToken, orgTelegramChatId);
                log.info("✅ Telegram notification sent to ORGANIZATION bot for order #{}", orderData.get("orderId"));
            } catch (Exception e) {
                log.error("❌ Failed to send Telegram notification to ORGANIZATION bot", e);
            }
        } else {
            log.debug("ℹ️ Organization bot is not configured for this order. Skipping organization bot notification.");
        }
    }

    /**
     * Build order message text
     */
    private String buildOrderMessage(Map<String, Object> orderData) {
        StringBuilder message = new StringBuilder();
        message.append("🛒 *НОВЫЙ ЗАКАЗ*\n\n");
        
        // Organization and domain info
        if (orderData.containsKey("organizationName") && orderData.get("organizationName") != null) {
            message.append(String.format("🏢 Организация: %s\n", orderData.get("organizationName")));
        }
        if (orderData.containsKey("domain") && orderData.get("domain") != null) {
            message.append(String.format("🌐 Домен: %s\n", orderData.get("domain")));
        }
        if (orderData.containsKey("organizationName") || orderData.containsKey("domain")) {
            message.append("\n");
        }
        
        message.append(String.format("📋 Заказ #%s\n", orderData.get("orderId")));
        message.append(String.format("👤 Клиент: %s\n", orderData.get("customerName")));
        message.append(String.format("📞 Телефон: %s\n", orderData.get("customerPhone")));
        
        if (orderData.containsKey("customerEmail") && orderData.get("customerEmail") != null) {
            message.append(String.format("✉️ Email: %s\n", orderData.get("customerEmail")));
        }
        
        if (orderData.containsKey("deliveryAddress") && orderData.get("deliveryAddress") != null) {
            message.append(String.format("🚚 Адрес: %s\n", orderData.get("deliveryAddress")));
        }
        
        message.append(String.format("\n💰 Сумма: %s руб\n\n", orderData.get("totalAmount")));
        
        // Add items
        if (orderData.containsKey("items")) {
            message.append("📦 *Товары:*\n");
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) orderData.get("items");
            for (Map<String, Object> item : items) {
                message.append(String.format("  • %s × %s = %s руб\n", 
                    item.get("productName"), 
                    item.get("quantity"),
                    item.get("totalPrice")));
            }
        }
        
        if (orderData.containsKey("comment") && orderData.get("comment") != null) {
            message.append(String.format("\n💬 Комментарий: %s\n", orderData.get("comment")));
        }
        
        message.append("\n🕐 " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        
        return message.toString();
    }

    /**
     * Send message to Telegram bot
     * 
     * @param message Message text
     * @param token Telegram bot token
     * @param chatId Chat ID to send to
     */
    private void sendMessage(String message, String token, String chatId) {
        String url = String.format("https://api.telegram.org/bot%s/sendMessage", token);
        
        Map<String, Object> request = new HashMap<>();
        request.put("chat_id", chatId);
        request.put("text", message);
        request.put("parse_mode", "Markdown");
        
        WebClient webClient = webClientBuilder.baseUrl(url).build();
        
        webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("Telegram API error for chat {}: {}", chatId, e.getMessage());
                    return Mono.empty();
                })
                .subscribe(response -> log.debug("Telegram response: {}", response));
    }
    
    /**
     * Send message to Telegram (legacy method for main bot)
     */
    private void sendMessage(String message) {
        sendMessage(message, botToken, chatId);
    }

    /**
     * Send test message
     */
    public boolean sendTestMessage() {
        if (!enabled || botToken.isEmpty() || chatId.isEmpty()) {
            log.warn("Telegram notifications are not configured");
            return false;
        }

        try {
            sendMessage("✅ Тест уведомлений Klassifikator\n\nСистема уведомлений работает!");
            return true;
        } catch (Exception e) {
            log.error("Failed to send test message", e);
            return false;
        }
    }
}

