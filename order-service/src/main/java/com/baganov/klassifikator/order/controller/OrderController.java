/**
 * @file: OrderController.java
 * @description: REST controller for order management
 * @dependencies: Spring Web
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.controller;

import com.baganov.klassifikator.order.model.enums.OrderStatus;
import com.baganov.klassifikator.order.model.dto.OrderDto;
import com.baganov.klassifikator.order.model.dto.OrderRequestDto;
import com.baganov.klassifikator.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Create a new order
     * POST /api/v1/orders
     */
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequestDto request) {
        log.info("POST /api/v1/orders - Creating order for organization {}", request.getOrganizationId());
        OrderDto order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    /**
     * Get order by ID
     * GET /api/v1/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        log.info("GET /api/v1/orders/{} - Getting order", id);
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Get orders by organization
     * GET /api/v1/orders/organization/{organizationId}
     */
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<Page<OrderDto>> getOrdersByOrganization(
            @PathVariable Long organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("GET /api/v1/orders/organization/{} - Getting orders", organizationId);
        
        Sort sort = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderDto> orders = orderService.getOrdersByOrganization(organizationId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get orders by landing
     * GET /api/v1/orders/landing/{landingId}
     */
    @GetMapping("/landing/{landingId}")
    public ResponseEntity<Page<OrderDto>> getOrdersByLanding(
            @PathVariable Long landingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("GET /api/v1/orders/landing/{} - Getting orders", landingId);
        
        Sort sort = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderDto> orders = orderService.getOrdersByLanding(landingId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get orders by status
     * GET /api/v1/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderDto>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("GET /api/v1/orders/status/{} - Getting orders", status);
        
        Sort sort = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderDto> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Update order status
     * PATCH /api/v1/orders/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        log.info("PATCH /api/v1/orders/{}/status - Updating status to {}", id, status);
        OrderDto order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Cancel order
     * POST /api/v1/orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long id) {
        log.info("POST /api/v1/orders/{}/cancel - Cancelling order", id);
        OrderDto order = orderService.cancelOrder(id);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Delete order
     * DELETE /api/v1/orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        log.info("DELETE /api/v1/orders/{} - Deleting order", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}

