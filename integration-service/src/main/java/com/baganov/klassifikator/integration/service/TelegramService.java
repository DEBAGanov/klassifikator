/**
 * @file: TelegramService.java
 * @description: Service interface for Telegram Bot integration
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.integration.service;

import com.baganov.klassifikator.integration.model.dto.TelegramNotificationDto;

public interface TelegramService {

    void sendNotification(TelegramNotificationDto notification);

    void sendOrderNotification(Long orderId, Long organizationId);
}

