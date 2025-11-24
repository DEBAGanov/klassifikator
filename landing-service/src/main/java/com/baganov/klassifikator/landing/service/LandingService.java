/**
 * @file: LandingService.java
 * @description: Service interface for Landing operations
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing.service;

import com.baganov.klassifikator.landing.model.dto.LandingRequestDto;
import com.baganov.klassifikator.landing.model.dto.LandingResponseDto;

import java.util.List;

public interface LandingService {

    /**
     * Create a new landing
     *
     * @param request landing creation request
     * @return created landing
     */
    LandingResponseDto createLanding(LandingRequestDto request);

    /**
     * Get landing by ID
     *
     * @param id landing ID
     * @return landing details
     */
    LandingResponseDto getLandingById(Long id);

    /**
     * Get landing by domain
     *
     * @param domain full domain name
     * @return landing details
     */
    LandingResponseDto getLandingByDomain(String domain);

    /**
     * Get landing by subdomain
     *
     * @param subdomain subdomain part (e.g. "modernissimo")
     * @return landing details
     */
    LandingResponseDto getLandingBySubdomain(String subdomain);

    /**
     * Get all landings for organization
     *
     * @param organizationId organization ID
     * @return list of landings
     */
    List<LandingResponseDto> getLandingsByOrganization(Long organizationId);

    /**
     * Get all landings
     *
     * @return list of all landings
     */
    List<LandingResponseDto> getAllLandings();

    /**
     * Update landing
     *
     * @param id landing ID
     * @param request update request
     * @return updated landing
     */
    LandingResponseDto updateLanding(Long id, LandingRequestDto request);

    /**
     * Delete landing
     *
     * @param id landing ID
     */
    void deleteLanding(Long id);

    /**
     * Publish landing
     *
     * @param id landing ID
     * @return published landing
     */
    LandingResponseDto publishLanding(Long id);
}

