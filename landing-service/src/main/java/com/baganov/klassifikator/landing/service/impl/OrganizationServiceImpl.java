/**
 * @file: OrganizationServiceImpl.java
 * @description: Implementation of OrganizationService
 * @dependencies: Spring, MapStruct
 * @created: 2025-11-04
 */
package com.baganov.klassifikator.landing.service.impl;

import com.baganov.klassifikator.common.model.entity.Organization;
import com.baganov.klassifikator.landing.model.dto.OrganizationDto;
import com.baganov.klassifikator.landing.model.mapper.OrganizationMapper;
import com.baganov.klassifikator.landing.repository.OrganizationRepository;
import com.baganov.klassifikator.landing.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    @Transactional
    public OrganizationDto createOrganization(OrganizationDto dto) {
        log.info("Creating organization: {}", dto.getName());

        Organization entity = organizationMapper.toEntity(dto);
        Organization saved = organizationRepository.save(entity);

        log.info("Created organization with ID: {}", saved.getId());
        return organizationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDto getOrganization(Long id) {
        log.info("Getting organization by ID: {}", id);

        Organization entity = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + id));

        return organizationMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationDto> getAllOrganizations() {
        log.info("Getting all organizations");

        return organizationRepository.findAll().stream()
                .map(organizationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrganizationDto updateOrganization(Long id, OrganizationDto dto) {
        log.info("Updating organization with ID: {}", id);

        Organization entity = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + id));

        // Update fields
        entity.setName(dto.getName());
        entity.setCategory(dto.getCategory());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setStatus(dto.getStatus());
        entity.setWorkingHours(dto.getWorkingHours());
        entity.setWebsite(dto.getWebsite());

        Organization updated = organizationRepository.save(entity);

        log.info("Updated organization with ID: {}", id);
        return organizationMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteOrganization(Long id) {
        log.info("Deleting organization with ID: {}", id);

        if (!organizationRepository.existsById(id)) {
            throw new RuntimeException("Organization not found with ID: " + id);
        }

        organizationRepository.deleteById(id);
        log.info("Deleted organization with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDto getOrganizationByName(String name) {
        log.info("Getting organization by name: {}", name);

        Organization entity = organizationRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Organization not found with name: " + name));

        return organizationMapper.toDto(entity);
    }
}

