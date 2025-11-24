/**
 * @file: SeoDataRepository.java
 * @description: Repository for SeoData entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-22
 */
package com.baganov.klassifikator.landing.repository;

import com.baganov.klassifikator.common.model.entity.SeoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeoDataRepository extends JpaRepository<SeoData, Long> {

    /**
     * Find SEO data by landing ID
     *
     * @param landingId landing ID
     * @return optional SEO data
     */
    Optional<SeoData> findByLandingId(Long landingId);

    /**
     * Check if SEO data exists for landing
     *
     * @param landingId landing ID
     * @return true if exists
     */
    boolean existsByLandingId(Long landingId);

    /**
     * Delete SEO data by landing ID
     *
     * @param landingId landing ID
     */
    void deleteByLandingId(Long landingId);
}

