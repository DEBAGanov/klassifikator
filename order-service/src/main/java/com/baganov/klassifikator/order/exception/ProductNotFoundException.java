/**
 * @file: ProductNotFoundException.java
 * @description: Exception thrown when product is not found
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.exception;

public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(Long id) {
        super(String.format("Product with id %d not found", id));
    }
    
    public ProductNotFoundException(String message) {
        super(message);
    }
}

