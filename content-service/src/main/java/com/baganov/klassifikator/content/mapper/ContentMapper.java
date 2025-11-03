/**
 * @file: ContentMapper.java
 * @description: Mapper for OrganizationContent entity and DTOs
 * @dependencies: MapStruct
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.mapper;

import com.baganov.klassifikator.common.model.entity.OrganizationContent;
import com.baganov.klassifikator.content.model.dto.ContentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ContentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    OrganizationContent toEntity(ContentDto dto);

    ContentDto toDto(OrganizationContent entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    void updateEntity(ContentDto dto, @MappingTarget OrganizationContent entity);
}

