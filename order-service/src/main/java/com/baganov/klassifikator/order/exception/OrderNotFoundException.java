/**
 * @file: OrderNotFoundException.java
 * @description: Exception thrown when order is not found
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.exception;

public class OrderNotFoundException extends RuntimeException {
    
    public OrderNotFoundException(Long id) {
        super(String.format("Order with id %d not found", id));
    }
    
    public OrderNotFoundException(String message) {
        super(message);
    }
}

