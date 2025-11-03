/**
 * @file: MediaService.java
 * @description: Service interface for Media operations
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.media.service;

import com.baganov.klassifikator.media.model.dto.MediaFileDto;
import com.baganov.klassifikator.media.model.dto.UploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {

    UploadResponseDto uploadFile(MultipartFile file, Long organizationId);

    MediaFileDto getFileById(Long id);

    List<MediaFileDto> getFilesByOrganization(Long organizationId);

    String getFileUrl(Long fileId);

    void deleteFile(Long id);

    UploadResponseDto uploadFromUrl(String imageUrl, Long organizationId, String imageName);
}

