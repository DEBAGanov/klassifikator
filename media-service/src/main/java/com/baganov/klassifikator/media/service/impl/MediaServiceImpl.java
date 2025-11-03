/**
 * @file: MediaServiceImpl.java
 * @description: Implementation of MediaService with S3 integration
 * @dependencies: Spring, AWS SDK, Lombok
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.media.service.impl;

import com.baganov.klassifikator.common.model.entity.MediaFile;
import com.baganov.klassifikator.media.mapper.MediaFileMapper;
import com.baganov.klassifikator.media.model.dto.MediaFileDto;
import com.baganov.klassifikator.media.model.dto.UploadResponseDto;
import com.baganov.klassifikator.media.repository.MediaFileRepository;
import com.baganov.klassifikator.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final MediaFileMapper mediaFileMapper;
    private final S3Client s3Client;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Value("${s3.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    @CacheEvict(value = "media", allEntries = true)
    public UploadResponseDto uploadFile(MultipartFile file, Long organizationId) {
        log.info("Uploading file {} for organization {}", file.getOriginalFilename(), organizationId);

        try {
            String fileName = file.getOriginalFilename();
            String fileType = file.getContentType();
            Long fileSize = file.getSize();

            // Generate unique S3 key
            String s3Key = String.format("organizations/%d/%s_%s",
                    organizationId,
                    UUID.randomUUID(),
                    fileName);

            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(fileType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // Save metadata to database
            MediaFile mediaFile = new MediaFile();
            mediaFile.setOrganizationId(organizationId);
            mediaFile.setFilename(s3Key);
            mediaFile.setOriginalFilename(fileName);
            mediaFile.setFilePath(baseUrl + "/" + s3Key);
            mediaFile.setMimeType(fileType);
            mediaFile.setFileSize(fileSize);

            MediaFile saved = mediaFileRepository.save(mediaFile);

            log.info("Successfully uploaded file with id {}", saved.getId());

            return UploadResponseDto.builder()
                    .fileId(saved.getId())
                    .fileName(fileName)
                    .fileUrl(saved.getFilePath())
                    .fileSize(fileSize)
                    .message("File uploaded successfully")
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload file for organization {}", organizationId, e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "media", key = "#id")
    public MediaFileDto getFileById(Long id) {
        log.debug("Fetching media file with id {}", id);

        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media file not found with id: " + id));

        return mediaFileMapper.toDto(mediaFile);
    }

    @Override
    @Cacheable(value = "media", key = "'org_' + #organizationId")
    public List<MediaFileDto> getFilesByOrganization(Long organizationId) {
        log.debug("Fetching media files for organization {}", organizationId);

        List<MediaFile> files = mediaFileRepository.findByOrganizationId(organizationId);
        return mediaFileMapper.toDtoList(files);
    }

    @Override
    public String getFileUrl(Long fileId) {
        log.debug("Getting URL for file {}", fileId);

        MediaFile mediaFile = mediaFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Media file not found with id: " + fileId));

        return mediaFile.getFilePath();
    }

    @Override
    @Transactional
    @CacheEvict(value = "media", allEntries = true)
    public void deleteFile(Long id) {
        log.info("Deleting media file with id {}", id);

        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media file not found with id: " + id));

        // Delete from S3
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(mediaFile.getFilename())
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", mediaFile.getFilename(), e);
        }

        // Delete from database
        mediaFileRepository.deleteById(id);
        log.info("Successfully deleted media file with id {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "media", allEntries = true)
    public UploadResponseDto uploadFromUrl(String imageUrl, Long organizationId, String imageName) {
        log.info("Uploading image from URL {} for organization {}", imageUrl, organizationId);

        try {
            // Download image from URL
            java.net.URL url = new java.net.URL(imageUrl);
            java.io.InputStream inputStream = url.openStream();
            byte[] imageBytes = inputStream.readAllBytes();
            inputStream.close();

            // Determine file type from URL or content
            String fileType = "image/jpeg"; // default
            if (imageUrl.toLowerCase().endsWith(".png")) {
                fileType = "image/png";
            } else if (imageUrl.toLowerCase().endsWith(".gif")) {
                fileType = "image/gif";
            } else if (imageUrl.toLowerCase().endsWith(".webp")) {
                fileType = "image/webp";
            }

            // Extract filename from URL or use provided name
            String fileName = imageName;
            if (!fileName.contains(".")) {
                String extension = imageUrl.substring(imageUrl.lastIndexOf("."));
                if (extension.length() > 5) {
                    extension = ".jpg";
                }
                fileName = fileName + extension;
            }

            // Generate unique S3 key
            String s3Key = String.format("organizations/%d/%s_%s",
                    organizationId,
                    UUID.randomUUID(),
                    fileName);

            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(fileType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

            // Save metadata to database
            MediaFile mediaFile = new MediaFile();
            mediaFile.setOrganizationId(organizationId);
            mediaFile.setFilename(s3Key);
            mediaFile.setOriginalFilename(fileName);
            mediaFile.setFilePath(baseUrl + "/" + s3Key);
            mediaFile.setMimeType(fileType);
            mediaFile.setFileSize((long) imageBytes.length);

            MediaFile saved = mediaFileRepository.save(mediaFile);

            log.info("Successfully uploaded image from URL with id {}", saved.getId());

            return UploadResponseDto.builder()
                    .fileId(saved.getId())
                    .fileName(fileName)
                    .fileUrl(saved.getFilePath())
                    .fileSize((long) imageBytes.length)
                    .message("Image uploaded successfully from URL")
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload image from URL for organization {}", organizationId, e);
            throw new RuntimeException("Failed to upload image from URL: " + e.getMessage());
        }
    }
}

