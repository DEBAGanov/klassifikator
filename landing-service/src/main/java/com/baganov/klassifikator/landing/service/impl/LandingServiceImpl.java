/**
 * @file: LandingServiceImpl.java
 * @description: Implementation of LandingService
 * @dependencies: Spring, Lombok
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing.service.impl;

import com.baganov.klassifikator.common.model.entity.Landing;
import com.baganov.klassifikator.landing.mapper.LandingMapper;
import com.baganov.klassifikator.landing.model.dto.LandingRequestDto;
import com.baganov.klassifikator.landing.model.dto.LandingResponseDto;
import com.baganov.klassifikator.landing.repository.LandingRepository;
import com.baganov.klassifikator.landing.repository.OrganizationRepository;
import com.baganov.klassifikator.landing.service.LandingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandingServiceImpl implements LandingService {

    private final LandingRepository landingRepository;
    private final OrganizationRepository organizationRepository;
    private final LandingMapper landingMapper;

    @Override
    @Transactional
    @CacheEvict(value = "landing", allEntries = true)
    public LandingResponseDto createLanding(LandingRequestDto request) {
        log.info("Creating landing for organization {} with subdomain {}", 
                 request.getOrganizationId(), request.getSubdomain());

        // Validate organization exists
        if (!organizationRepository.existsById(request.getOrganizationId())) {
            throw new RuntimeException("Organization not found with id: " + request.getOrganizationId());
        }

        // Check subdomain uniqueness
        if (landingRepository.existsBySubdomain(request.getSubdomain())) {
            throw new RuntimeException("Subdomain already exists: " + request.getSubdomain());
        }

        Landing landing = landingMapper.toEntity(request);
        landing.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");
        landing.setSslEnabled(false); // Will be enabled manually by admin

        Landing savedLanding = landingRepository.save(landing);
        log.info("Successfully created landing with id {}", savedLanding.getId());

        return landingMapper.toDto(savedLanding);
    }

    @Override
    @Cacheable(value = "landing", key = "#id")
    public LandingResponseDto getLandingById(Long id) {
        log.debug("Fetching landing with id {}", id);
        
        Landing landing = landingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Landing not found with id: " + id));
        
        return landingMapper.toDto(landing);
    }

    @Override
    @Cacheable(value = "landing", key = "#domain")
    public LandingResponseDto getLandingByDomain(String domain) {
        log.debug("Fetching landing with domain {}", domain);
        
        Landing landing = landingRepository.findByDomain(domain)
                .orElseThrow(() -> new RuntimeException("Landing not found with domain: " + domain));
        
        return landingMapper.toDto(landing);
    }

    @Override
    public List<LandingResponseDto> getLandingsByOrganization(Long organizationId) {
        log.debug("Fetching landings for organization {}", organizationId);
        
        return landingRepository.findByOrganizationId(organizationId).stream()
                .map(landingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LandingResponseDto> getAllLandings() {
        log.debug("Fetching all landings");
        
        return landingRepository.findAll().stream()
                .map(landingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "landing", key = "#id")
    public LandingResponseDto updateLanding(Long id, LandingRequestDto request) {
        log.info("Updating landing with id {}", id);
        
        Landing landing = landingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Landing not found with id: " + id));

        // Check subdomain uniqueness if changed
        if (!landing.getSubdomain().equals(request.getSubdomain()) &&
            landingRepository.existsBySubdomain(request.getSubdomain())) {
            throw new RuntimeException("Subdomain already exists: " + request.getSubdomain());
        }

        landingMapper.updateEntity(request, landing);
        Landing updatedLanding = landingRepository.save(landing);
        
        log.info("Successfully updated landing with id {}", id);
        return landingMapper.toDto(updatedLanding);
    }

    @Override
    @Transactional
    @CacheEvict(value = "landing", key = "#id")
    public void deleteLanding(Long id) {
        log.info("Deleting landing with id {}", id);
        
        if (!landingRepository.existsById(id)) {
            throw new RuntimeException("Landing not found with id: " + id);
        }
        
        landingRepository.deleteById(id);
        log.info("Successfully deleted landing with id {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "landing", key = "#id")
    public LandingResponseDto publishLanding(Long id) {
        log.info("Publishing landing with id {}", id);
        
        Landing landing = landingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Landing not found with id: " + id));

        landing.setStatus("ACTIVE");
        landing.setPublishedAt(LocalDateTime.now());
        
        Landing publishedLanding = landingRepository.save(landing);
        log.info("Successfully published landing with id {}", id);
        
        return landingMapper.toDto(publishedLanding);
    }
}

