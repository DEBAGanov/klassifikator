/**
 * @file: TemplateMapper.java
 * @description: Mapper for Template entity and DTOs
 * @dependencies: MapStruct
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template.mapper;

import com.baganov.klassifikator.common.model.entity.Template;
import com.baganov.klassifikator.template.model.dto.TemplateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TemplateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Template toEntity(TemplateDto dto);

    TemplateDto toDto(Template entity);

    List<TemplateDto> toDtoList(List<Template> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TemplateDto dto, @MappingTarget Template entity);
}

