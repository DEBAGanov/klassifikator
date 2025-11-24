/**
 * @file: OrganizationMapper.java
 * @description: MapStruct mapper for Organization entity
 * @dependencies: MapStruct
 * @created: 2025-11-04
 */
package com.baganov.klassifikator.landing.model.mapper;

import com.baganov.klassifikator.common.model.entity.Organization;
import com.baganov.klassifikator.landing.model.dto.OrganizationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Organization toEntity(OrganizationDto dto);

    OrganizationDto toDto(Organization entity);
}

