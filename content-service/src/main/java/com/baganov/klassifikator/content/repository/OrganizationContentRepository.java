/**
 * @file: OrganizationContentRepository.java
 * @description: Repository for OrganizationContent entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.repository;

import com.baganov.klassifikator.common.model.entity.OrganizationContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationContentRepository extends JpaRepository<OrganizationContent, Long> {
    
    Optional<OrganizationContent> findByOrganizationId(Long organizationId);
    
    boolean existsByOrganizationId(Long organizationId);
}

