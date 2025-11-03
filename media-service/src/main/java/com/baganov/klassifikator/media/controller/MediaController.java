/**
 * @file: MediaController.java
 * @description: REST controller for Media operations
 * @dependencies: Spring Web
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.media.controller;

import com.baganov.klassifikator.media.model.dto.MediaFileDto;
import com.baganov.klassifikator.media.model.dto.UploadResponseDto;
import com.baganov.klassifikator.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("organizationId") Long organizationId) {
        log.info("POST /api/v1/media/upload - Uploading file for organization {}", organizationId);
        UploadResponseDto response = mediaService.uploadFile(file, organizationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaFileDto> getFileById(@PathVariable Long id) {
        log.info("GET /api/v1/media/{}", id);
        MediaFileDto response = mediaService.getFileById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<MediaFileDto>> getFilesByOrganization(@PathVariable Long organizationId) {
        log.info("GET /api/v1/media/organization/{}", organizationId);
        List<MediaFileDto> response = mediaService.getFilesByOrganization(organizationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/url")
    public ResponseEntity<String> getFileUrl(@PathVariable Long id) {
        log.info("GET /api/v1/media/{}/url", id);
        String url = mediaService.getFileUrl(id);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        log.info("DELETE /api/v1/media/{}", id);
        mediaService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload-from-url")
    public ResponseEntity<UploadResponseDto> uploadFromUrl(
            @RequestBody java.util.Map<String, Object> request) {
        log.info("POST /api/v1/media/upload-from-url - Uploading from URL");
        
        String imageUrl = (String) request.get("imageUrl");
        Long organizationId = Long.valueOf(request.get("organizationId").toString());
        String imageName = (String) request.getOrDefault("imageName", "image");
        
        UploadResponseDto response = mediaService.uploadFromUrl(imageUrl, organizationId, imageName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

