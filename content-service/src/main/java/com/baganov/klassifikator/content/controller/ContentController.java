/**
 * @file: ContentController.java
 * @description: REST controller for Content operations
 * @dependencies: Spring Web
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.controller;

import com.baganov.klassifikator.content.model.dto.ContentDto;
import com.baganov.klassifikator.content.model.dto.FullContentDto;
import com.baganov.klassifikator.content.model.dto.ProductDto;
import com.baganov.klassifikator.content.model.dto.PromotionDto;
import com.baganov.klassifikator.content.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    /**
     * Get full content for organization
     */
    @GetMapping("/organization/{organizationId}/full")
    public ResponseEntity<FullContentDto> getFullContent(@PathVariable Long organizationId) {
        log.info("GET /api/v1/content/organization/{}/full", organizationId);
        FullContentDto response = contentService.getFullContent(organizationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get content by organization
     */
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<ContentDto> getContentByOrganization(@PathVariable Long organizationId) {
        log.info("GET /api/v1/content/organization/{}", organizationId);
        ContentDto response = contentService.getContentByOrganization(organizationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Save content
     */
    @PostMapping
    public ResponseEntity<ContentDto> saveContent(@Valid @RequestBody ContentDto dto) {
        log.info("POST /api/v1/content - Saving content for organization {}", dto.getOrganizationId());
        ContentDto response = contentService.saveContent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get products for organization
     */
    @GetMapping("/organization/{organizationId}/products")
    public ResponseEntity<List<ProductDto>> getProducts(
            @PathVariable Long organizationId,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        log.info("GET /api/v1/content/organization/{}/products?activeOnly={}", organizationId, activeOnly);
        List<ProductDto> response = activeOnly 
                ? contentService.getActiveProducts(organizationId)
                : contentService.getProducts(organizationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Create product
     */
    @PostMapping("/products")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto dto) {
        log.info("POST /api/v1/content/products - Creating product");
        ProductDto response = contentService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update product
     */
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto dto) {
        log.info("PUT /api/v1/content/products/{}", id);
        ProductDto response = contentService.updateProduct(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete product
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/v1/content/products/{}", id);
        contentService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get promotions for organization
     */
    @GetMapping("/organization/{organizationId}/promotions")
    public ResponseEntity<List<PromotionDto>> getPromotions(
            @PathVariable Long organizationId,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        log.info("GET /api/v1/content/organization/{}/promotions?activeOnly={}", organizationId, activeOnly);
        List<PromotionDto> response = activeOnly
                ? contentService.getActivePromotions(organizationId)
                : contentService.getPromotions(organizationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Create promotion
     */
    @PostMapping("/promotions")
    public ResponseEntity<PromotionDto> createPromotion(@Valid @RequestBody PromotionDto dto) {
        log.info("POST /api/v1/content/promotions - Creating promotion");
        PromotionDto response = contentService.createPromotion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update promotion
     */
    @PutMapping("/promotions/{id}")
    public ResponseEntity<PromotionDto> updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody PromotionDto dto) {
        log.info("PUT /api/v1/content/promotions/{}", id);
        PromotionDto response = contentService.updatePromotion(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete promotion
     */
    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        log.info("DELETE /api/v1/content/promotions/{}", id);
        contentService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}

