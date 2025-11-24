/**
 * @file: OrganizationRepository.java
 * @description: Repository for Organization entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing.repository;

import com.baganov.klassifikator.common.model.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    List<Organization> findByStatus(String status);
    
    List<Organization> findByCategory(String category);
    
    Optional<Organization> findByName(String name);
    
    boolean existsByName(String name);
}

