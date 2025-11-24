/**
 * @file: LandingController.java
 * @description: REST controller for Landing operations
 * @dependencies: Spring Web
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing.controller;

import com.baganov.klassifikator.landing.model.dto.LandingRequestDto;
import com.baganov.klassifikator.landing.model.dto.LandingResponseDto;
import com.baganov.klassifikator.landing.service.LandingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/landings")
@RequiredArgsConstructor
public class LandingController {

    private final LandingService landingService;

    /**
     * Create a new landing
     */
    @PostMapping
    public ResponseEntity<LandingResponseDto> createLanding(@Valid @RequestBody LandingRequestDto request) {
        log.info("POST /api/v1/landings - Creating landing for organization {}", request.getOrganizationId());
        LandingResponseDto response = landingService.createLanding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get landing by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LandingResponseDto> getLandingById(@PathVariable Long id) {
        log.info("GET /api/v1/landings/{} - Fetching landing", id);
        LandingResponseDto response = landingService.getLandingById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get landing by domain
     */
    @GetMapping("/domain/{domain}")
    public ResponseEntity<LandingResponseDto> getLandingByDomain(@PathVariable String domain) {
        log.info("GET /api/v1/landings/domain/{} - Fetching landing", domain);
        LandingResponseDto response = landingService.getLandingByDomain(domain);
        return ResponseEntity.ok(response);
    }

    /**
     * Get landing by subdomain
     */
    @GetMapping("/by-subdomain/{subdomain}")
    public ResponseEntity<LandingResponseDto> getLandingBySubdomain(@PathVariable String subdomain) {
        log.info("GET /api/v1/landings/by-subdomain/{} - Fetching landing", subdomain);
        LandingResponseDto response = landingService.getLandingBySubdomain(subdomain);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all landings for organization
     */
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<LandingResponseDto>> getLandingsByOrganization(@PathVariable Long organizationId) {
        log.info("GET /api/v1/landings/organization/{} - Fetching landings", organizationId);
        List<LandingResponseDto> response = landingService.getLandingsByOrganization(organizationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all landings
     */
    @GetMapping
    public ResponseEntity<List<LandingResponseDto>> getAllLandings() {
        log.info("GET /api/v1/landings - Fetching all landings");
        List<LandingResponseDto> response = landingService.getAllLandings();
        return ResponseEntity.ok(response);
    }

    /**
     * Update landing
     */
    @PutMapping("/{id}")
    public ResponseEntity<LandingResponseDto> updateLanding(
            @PathVariable Long id,
            @Valid @RequestBody LandingRequestDto request) {
        log.info("PUT /api/v1/landings/{} - Updating landing", id);
        LandingResponseDto response = landingService.updateLanding(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete landing
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLanding(@PathVariable Long id) {
        log.info("DELETE /api/v1/landings/{} - Deleting landing", id);
        landingService.deleteLanding(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publish landing
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<LandingResponseDto> publishLanding(@PathVariable Long id) {
        log.info("POST /api/v1/landings/{}/publish - Publishing landing", id);
        LandingResponseDto response = landingService.publishLanding(id);
        return ResponseEntity.ok(response);
    }
}

