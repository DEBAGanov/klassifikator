/**
 * @file: Organization.java
 * @description: Organization entity representing businesses/companies
 * @dependencies: JPA, Lombok
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.common.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String website;

    @Column(name = "working_hours", columnDefinition = "TEXT")
    private String workingHours;

    @Column(length = 50)
    private String status = "ACTIVE";

    @Column(name = "google_sheet_id", length = 255)
    private String googleSheetId;

    @Column(name = "telegram_bot_token", length = 500)
    private String telegramBotToken;

    @Column(name = "telegram_chat_id", length = 100)
    private String telegramChatId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

