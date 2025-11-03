/**
 * @file: PromotionRepository.java
 * @description: Repository for Promotion entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.repository;

import com.baganov.klassifikator.common.model.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    List<Promotion> findByOrganizationId(Long organizationId);
    
    List<Promotion> findByOrganizationIdAndIsActive(Long organizationId, Boolean isActive);
    
    @Query("SELECT p FROM Promotion p WHERE p.organizationId = :organizationId " +
           "AND p.isActive = true " +
           "AND (p.startDate IS NULL OR p.startDate <= :today) " +
           "AND (p.endDate IS NULL OR p.endDate >= :today)")
    List<Promotion> findActivePromotions(Long organizationId, LocalDate today);
}

