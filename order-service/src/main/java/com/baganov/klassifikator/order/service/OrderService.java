/**
 * @file: OrderService.java
 * @description: Service interface for order management
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.service;

import com.baganov.klassifikator.order.model.enums.OrderStatus;
import com.baganov.klassifikator.order.model.dto.OrderDto;
import com.baganov.klassifikator.order.model.dto.OrderRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    
    /**
     * Create a new order
     */
    OrderDto createOrder(OrderRequestDto request);
    
    /**
     * Get order by ID
     */
    OrderDto getOrderById(Long id);
    
    /**
     * Get all orders by organization
     */
    Page<OrderDto> getOrdersByOrganization(Long organizationId, Pageable pageable);
    
    /**
     * Get all orders by landing
     */
    Page<OrderDto> getOrdersByLanding(Long landingId, Pageable pageable);
    
    /**
     * Get all orders by status
     */
    Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * Update order status
     */
    OrderDto updateOrderStatus(Long id, OrderStatus status);
    
    /**
     * Cancel order
     */
    OrderDto cancelOrder(Long id);
    
    /**
     * Delete order
     */
    void deleteOrder(Long id);
}

