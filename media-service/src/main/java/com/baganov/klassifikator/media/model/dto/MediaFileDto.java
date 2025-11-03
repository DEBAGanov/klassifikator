/**
 * @file: MediaFileDto.java
 * @description: DTO for media file
 * @dependencies: Jakarta Validation
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.media.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileDto {

    private Long id;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "File path is required")
    private String filePath;

    @NotBlank(message = "File type is required")
    private String fileType;

    private Long fileSize;
    private String s3Key;
    private String s3Bucket;
    private String altText;
    private LocalDateTime uploadedAt;
}

