/**
 * @file: OrganizationService.java
 * @description: Service interface for Organization operations
 * @dependencies: None
 * @created: 2025-11-04
 */
package com.baganov.klassifikator.landing.service;

import com.baganov.klassifikator.landing.model.dto.OrganizationDto;

import java.util.List;

public interface OrganizationService {

    OrganizationDto createOrganization(OrganizationDto dto);

    OrganizationDto getOrganization(Long id);

    List<OrganizationDto> getAllOrganizations();

    OrganizationDto updateOrganization(Long id, OrganizationDto dto);

    void deleteOrganization(Long id);

    OrganizationDto getOrganizationByName(String name);
}

