/**
 * @file: GoogleSheetsSyncRepository.java
 * @description: Repository for GoogleSheetsSync entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.integration.repository;

import com.baganov.klassifikator.common.model.entity.GoogleSheetsSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoogleSheetsSyncRepository extends JpaRepository<GoogleSheetsSync, Long> {
    
    Optional<GoogleSheetsSync> findByOrganizationId(Long organizationId);
    
    List<GoogleSheetsSync> findByIsActiveTrue();
}

