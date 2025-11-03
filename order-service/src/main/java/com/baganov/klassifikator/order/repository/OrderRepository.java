/**
 * @file: OrderRepository.java
 * @description: Repository for Order entity
 * @dependencies: Spring Data JPA
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.repository;

import com.baganov.klassifikator.common.model.entity.Order;
import com.baganov.klassifikator.order.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find all orders by organization ID
     */
    Page<Order> findByOrganizationId(Long organizationId, Pageable pageable);
    
    /**
     * Find all orders by landing ID
     */
    Page<Order> findByLandingId(Long landingId, Pageable pageable);
    
    /**
     * Find all orders by status
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * Find all orders by organization and status
     */
    Page<Order> findByOrganizationIdAndStatus(Long organizationId, OrderStatus status, Pageable pageable);
    
    /**
     * Find orders by customer phone
     */
    List<Order> findByCustomerPhone(String customerPhone);
    
    /**
     * Find orders by customer email
     */
    List<Order> findByCustomerEmail(String customerEmail);
    
    /**
     * Find orders created between dates
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Count orders by organization
     */
    long countByOrganizationId(Long organizationId);
    
    /**
     * Count orders by status
     */
    long countByStatus(OrderStatus status);
}

