/**
 * @file: TelegramNotificationDto.java
 * @description: DTO for Telegram notification
 * @dependencies: Jakarta Validation
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.integration.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramNotificationDto {

    @NotBlank(message = "Chat ID is required")
    private String chatId;

    @NotBlank(message = "Message is required")
    private String message;

    private String parseMode;
}

