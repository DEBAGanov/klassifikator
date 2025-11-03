/**
 * @file: OrderRequestDto.java
 * @description: DTO for creating order
 * @dependencies: Lombok, Jakarta Validation
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    
    @NotNull(message = "Organization ID is required")
    private Long organizationId;
    
    // Landing ID is optional - if not provided, domain will be taken from OrganizationContent
    private Long landingId;
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Customer phone is required")
    private String customerPhone;
    
    @Email(message = "Invalid email format")
    private String customerEmail;
    
    private String deliveryAddress;
    
    private String comment;
    
    @NotEmpty(message = "Order items are required")
    private List<OrderItemRequestDto> items;
}

