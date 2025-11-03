/**
 * @file: OrderMapper.java
 * @description: Mapper for Order entity and DTOs
 * @dependencies: MapStruct
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order.mapper;

import com.baganov.klassifikator.common.model.entity.Order;
import com.baganov.klassifikator.order.model.dto.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {OrderItemMapper.class}
)
public interface OrderMapper {
    
    /**
     * Convert Order entity to OrderDto
     */
    @Mapping(target = "items", source = "items")
    OrderDto toDto(Order order);
    
    /**
     * Convert OrderDto to Order entity
     */
    @Mapping(target = "items", source = "items")
    Order toEntity(OrderDto dto);
    
    /**
     * Convert list of Order entities to list of OrderDtos
     */
    List<OrderDto> toDtoList(List<Order> orders);
}

