/**
 * @file: PromotionMapper.java
 * @description: Mapper for Promotion entity and DTOs
 * @dependencies: MapStruct
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.mapper;

import com.baganov.klassifikator.common.model.entity.Promotion;
import com.baganov.klassifikator.content.model.dto.PromotionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "image", ignore = true)
    Promotion toEntity(PromotionDto dto);

    PromotionDto toDto(Promotion entity);

    List<PromotionDto> toDtoList(List<Promotion> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "image", ignore = true)
    void updateEntity(PromotionDto dto, @MappingTarget Promotion entity);
}

