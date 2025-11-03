/**
 * @file: ProductRepository.java
 * @description: Repository for Product entity (read-only for Order Service)
 * @dependencies: Spring Data JPA
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.repository;

import com.baganov.klassifikator.common.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Read-only access to products for order creation
}

