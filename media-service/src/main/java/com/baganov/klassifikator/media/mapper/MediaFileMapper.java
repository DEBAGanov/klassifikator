/**
 * @file: MediaFileMapper.java
 * @description: Mapper for MediaFile entity and DTOs
 * @dependencies: MapStruct
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.media.mapper;

import com.baganov.klassifikator.common.model.entity.MediaFile;
import com.baganov.klassifikator.media.model.dto.MediaFileDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MediaFileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    MediaFile toEntity(MediaFileDto dto);

    MediaFileDto toDto(MediaFile entity);

    List<MediaFileDto> toDtoList(List<MediaFile> entities);
}

