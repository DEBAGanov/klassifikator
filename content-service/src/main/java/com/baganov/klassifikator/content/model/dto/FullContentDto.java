/**
 * @file: FullContentDto.java
 * @description: DTO for full organization content including products and promotions
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FullContentDto {

    private ContentDto content;
    private List<ProductDto> products;
    private List<PromotionDto> promotions;
}

