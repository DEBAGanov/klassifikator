/**
 * @file: OrderItemMapper.java
 * @description: Mapper for OrderItem entity and DTOs
 * @dependencies: MapStruct
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.mapper;

import com.baganov.klassifikator.common.model.entity.OrderItem;
import com.baganov.klassifikator.order.model.dto.OrderItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemMapper {
    
    /**
     * Convert OrderItem entity to OrderItemDto
     * Map productPrice -> price and subtotal -> totalPrice
     */
    @Mapping(source = "productPrice", target = "price")
    @Mapping(source = "subtotal", target = "totalPrice")
    OrderItemDto toDto(OrderItem orderItem);
    
    /**
     * Convert OrderItemDto to OrderItem entity
     * Map price -> productPrice and totalPrice -> subtotal
     */
    @Mapping(source = "price", target = "productPrice")
    @Mapping(source = "totalPrice", target = "subtotal")
    OrderItem toEntity(OrderItemDto dto);
    
    /**
     * Convert list of OrderItem entities to list of OrderItemDtos
     */
    List<OrderItemDto> toDtoList(List<OrderItem> orderItems);
}

