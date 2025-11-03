/**
 * @file: OrganizationMapper.java
 * @description: Mapper for Organization entity and DTOs
 * @dependencies: MapStruct
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing.mapper;

import com.baganov.klassifikator.common.model.entity.Organization;
import com.baganov.klassifikator.landing.model.dto.OrganizationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Organization toEntity(OrganizationDto dto);

    OrganizationDto toDto(Organization entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(OrganizationDto dto, @MappingTarget Organization entity);
}

