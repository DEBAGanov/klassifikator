/**
 * @file: OrganizationContentRepository.java
 * @description: Repository for OrganizationContent entity (read-only)
 * @dependencies: Spring Data JPA
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.repository;

import com.baganov.klassifikator.common.model.entity.OrganizationContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationContentRepository extends JpaRepository<OrganizationContent, Long> {
    Optional<OrganizationContent> findByOrganizationId(Long organizationId);
}

