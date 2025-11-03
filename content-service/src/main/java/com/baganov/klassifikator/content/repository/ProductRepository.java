/**
 * @file: ProductRepository.java
 * @description: Repository for Product entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.repository;

import com.baganov.klassifikator.common.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByOrganizationId(Long organizationId);
    
    List<Product> findByOrganizationIdAndIsActive(Long organizationId, Boolean isActive);
    
    List<Product> findByOrganizationIdAndCategory(Long organizationId, String category);
}

