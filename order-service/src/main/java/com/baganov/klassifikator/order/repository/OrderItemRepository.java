/**
 * @file: OrderItemRepository.java
 * @description: Repository for OrderItem entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.repository;

import com.baganov.klassifikator.common.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find all items by order ID
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * Find all items by product ID
     */
    List<OrderItem> findByProductId(Long productId);
    
    /**
     * Count items by product ID
     */
    long countByProductId(Long productId);
    
    /**
     * Get total quantity sold for a product
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Long getTotalQuantitySoldByProduct(@Param("productId") Long productId);
}

