/**
 * @file: TemplateRepository.java
 * @description: Repository for Template entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template.repository;

import com.baganov.klassifikator.common.model.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    
    List<Template> findByIsActive(Boolean isActive);
    
    List<Template> findByNameContainingIgnoreCase(String name);
}

