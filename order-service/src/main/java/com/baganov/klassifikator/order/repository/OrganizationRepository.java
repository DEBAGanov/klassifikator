/**
 * @file: OrganizationRepository.java
 * @description: Repository for Organization entity (read-only)
 * @dependencies: Spring Data JPA
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.repository;

import com.baganov.klassifikator.common.model.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findById(Long id);
}

