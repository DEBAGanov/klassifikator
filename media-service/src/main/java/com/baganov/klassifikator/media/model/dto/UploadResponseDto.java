/**
 * @file: UploadResponseDto.java
 * @description: DTO for upload response
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.media.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponseDto {

    private Long fileId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String message;
}

