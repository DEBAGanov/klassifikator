/**
 * @file: LandingRepository.java
 * @description: Repository for Landing entity (read-only)
 * @dependencies: Spring Data JPA
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.repository;

import com.baganov.klassifikator.common.model.entity.Landing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LandingRepository extends JpaRepository<Landing, Long> {
    Optional<Landing> findById(Long id);
}

