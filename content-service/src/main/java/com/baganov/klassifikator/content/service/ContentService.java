/**
 * @file: ContentService.java
 * @description: Service interface for Content operations
 * @dependencies: None
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.service;

import com.baganov.klassifikator.content.model.dto.ContentDto;
import com.baganov.klassifikator.content.model.dto.FullContentDto;
import com.baganov.klassifikator.content.model.dto.ProductDto;
import com.baganov.klassifikator.content.model.dto.PromotionDto;

import java.util.List;

public interface ContentService {

    /**
     * Get full content for organization (content + products + promotions)
     *
     * @param organizationId organization ID
     * @return full content
     */
    FullContentDto getFullContent(Long organizationId);

    /**
     * Get content by organization ID
     *
     * @param organizationId organization ID
     * @return content
     */
    ContentDto getContentByOrganization(Long organizationId);

    /**
     * Create or update content
     *
     * @param dto content data
     * @return saved content
     */
    ContentDto saveContent(ContentDto dto);

    /**
     * Get all products for organization
     *
     * @param organizationId organization ID
     * @return list of products
     */
    List<ProductDto> getProducts(Long organizationId);

    /**
     * Get active products for organization
     *
     * @param organizationId organization ID
     * @return list of active products
     */
    List<ProductDto> getActiveProducts(Long organizationId);

    /**
     * Create product
     *
     * @param dto product data
     * @return created product
     */
    ProductDto createProduct(ProductDto dto);

    /**
     * Update product
     *
     * @param id product ID
     * @param dto product data
     * @return updated product
     */
    ProductDto updateProduct(Long id, ProductDto dto);

    /**
     * Delete product
     *
     * @param id product ID
     */
    void deleteProduct(Long id);

    /**
     * Get all promotions for organization
     *
     * @param organizationId organization ID
     * @return list of promotions
     */
    List<PromotionDto> getPromotions(Long organizationId);

    /**
     * Get active promotions for organization
     *
     * @param organizationId organization ID
     * @return list of active promotions
     */
    List<PromotionDto> getActivePromotions(Long organizationId);

    /**
     * Create promotion
     *
     * @param dto promotion data
     * @return created promotion
     */
    PromotionDto createPromotion(PromotionDto dto);

    /**
     * Update promotion
     *
     * @param id promotion ID
     * @param dto promotion data
     * @return updated promotion
     */
    PromotionDto updatePromotion(Long id, PromotionDto dto);

    /**
     * Delete promotion
     *
     * @param id promotion ID
     */
    void deletePromotion(Long id);
}

