/**
 * @file: LandingMapper.java
 * @description: Mapper for Landing entity and DTOs
 * @dependencies: MapStruct
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing.mapper;

import com.baganov.klassifikator.common.model.entity.Landing;
import com.baganov.klassifikator.landing.model.dto.LandingRequestDto;
import com.baganov.klassifikator.landing.model.dto.LandingResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LandingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "domain", expression = "java(dto.getSubdomain() + \".volzhck.ru\")")
    Landing toEntity(LandingRequestDto dto);

    LandingResponseDto toDto(Landing entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "domain", expression = "java(dto.getSubdomain() + \".volzhck.ru\")")
    void updateEntity(LandingRequestDto dto, @MappingTarget Landing entity);
}

