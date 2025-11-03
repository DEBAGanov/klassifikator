/**
 * @file: LandingRepository.java
 * @description: Repository for Landing entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing.repository;

import com.baganov.klassifikator.common.model.entity.Landing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LandingRepository extends JpaRepository<Landing, Long> {
    
    Optional<Landing> findByDomain(String domain);
    
    Optional<Landing> findBySubdomain(String subdomain);
    
    List<Landing> findByOrganizationId(Long organizationId);
    
    List<Landing> findByStatus(String status);
    
    boolean existsByDomain(String domain);
    
    boolean existsBySubdomain(String subdomain);
}

