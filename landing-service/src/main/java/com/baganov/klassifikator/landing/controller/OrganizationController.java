/**
 * @file: OrganizationController.java
 * @description: REST controller for Organization operations
 * @dependencies: Spring Web
 * @created: 2025-11-04
 */
package com.baganov.klassifikator.landing.controller;

import com.baganov.klassifikator.landing.model.dto.OrganizationDto;
import com.baganov.klassifikator.landing.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<OrganizationDto> createOrganization(@Valid @RequestBody OrganizationDto dto) {
        log.info("POST /api/v1/organizations - Creating organization");
        OrganizationDto response = organizationService.createOrganization(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDto> getOrganization(@PathVariable Long id) {
        log.info("GET /api/v1/organizations/{}", id);
        OrganizationDto response = organizationService.getOrganization(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationDto>> getAllOrganizations() {
        log.info("GET /api/v1/organizations");
        List<OrganizationDto> response = organizationService.getAllOrganizations();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationDto> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationDto dto) {
        log.info("PUT /api/v1/organizations/{}", id);
        OrganizationDto response = organizationService.updateOrganization(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
        log.info("DELETE /api/v1/organizations/{}", id);
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<OrganizationDto> getOrganizationByName(@PathVariable String name) {
        log.info("GET /api/v1/organizations/name/{}", name);
        OrganizationDto response = organizationService.getOrganizationByName(name);
        return ResponseEntity.ok(response);
    }
}

