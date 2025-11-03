/**
 * @file: MediaFileRepository.java
 * @description: Repository for MediaFile entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.media.repository;

import com.baganov.klassifikator.common.model.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    
    List<MediaFile> findByOrganizationId(Long organizationId);
    
    List<MediaFile> findByMimeType(String mimeType);
}

