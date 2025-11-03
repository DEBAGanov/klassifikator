/**
 * @file: ContentServiceImpl.java
 * @description: Implementation of ContentService
 * @dependencies: Spring, Lombok
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content.service.impl;

import com.baganov.klassifikator.common.model.entity.OrganizationContent;
import com.baganov.klassifikator.common.model.entity.Product;
import com.baganov.klassifikator.common.model.entity.Promotion;
import com.baganov.klassifikator.content.mapper.ContentMapper;
import com.baganov.klassifikator.content.mapper.ProductMapper;
import com.baganov.klassifikator.content.mapper.PromotionMapper;
import com.baganov.klassifikator.content.model.dto.ContentDto;
import com.baganov.klassifikator.content.model.dto.FullContentDto;
import com.baganov.klassifikator.content.model.dto.ProductDto;
import com.baganov.klassifikator.content.model.dto.PromotionDto;
import com.baganov.klassifikator.content.repository.OrganizationContentRepository;
import com.baganov.klassifikator.content.repository.ProductRepository;
import com.baganov.klassifikator.content.repository.PromotionRepository;
import com.baganov.klassifikator.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final OrganizationContentRepository contentRepository;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final ContentMapper contentMapper;
    private final ProductMapper productMapper;
    private final PromotionMapper promotionMapper;

    @Override
    // TODO: Fix caching for FullContentDto - currently causes ClassCastException with Redis
    // @Cacheable(value = "content", key = "#organizationId")
    public FullContentDto getFullContent(Long organizationId) {
        log.debug("Fetching full content for organization {}", organizationId);

        ContentDto content = getContentByOrganization(organizationId);
        List<ProductDto> products = getActiveProducts(organizationId);
        List<PromotionDto> promotions = getActivePromotions(organizationId);

        return FullContentDto.builder()
                .content(content)
                .products(products)
                .promotions(promotions)
                .build();
    }

    @Override
    @Cacheable(value = "content", key = "'org-' + #organizationId")
    public ContentDto getContentByOrganization(Long organizationId) {
        log.debug("Fetching content for organization {}", organizationId);

        OrganizationContent content = contentRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new RuntimeException("Content not found for organization: " + organizationId));

        return contentMapper.toDto(content);
    }

    @Override
    @Transactional
    @CacheEvict(value = "content", allEntries = true)
    public ContentDto saveContent(ContentDto dto) {
        log.info("Saving content for organization {}", dto.getOrganizationId());

        OrganizationContent content;
        if (contentRepository.existsByOrganizationId(dto.getOrganizationId())) {
            content = contentRepository.findByOrganizationId(dto.getOrganizationId())
                    .orElseThrow();
            contentMapper.updateEntity(dto, content);
            // Increment version, initialize to 1 if null
            Integer currentVersion = content.getVersion();
            content.setVersion(currentVersion != null ? currentVersion + 1 : 1);
        } else {
            content = contentMapper.toEntity(dto);
            // Set initial version for new content
            if (content.getVersion() == null) {
                content.setVersion(1);
            }
        }

        OrganizationContent saved = contentRepository.save(content);
        log.info("Successfully saved content for organization {}", dto.getOrganizationId());

        return contentMapper.toDto(saved);
    }

    @Override
    @Cacheable(value = "product", key = "'org-' + #organizationId")
    public List<ProductDto> getProducts(Long organizationId) {
        log.debug("Fetching products for organization {}", organizationId);

        List<Product> products = productRepository.findByOrganizationId(organizationId);
        return productMapper.toDtoList(products);
    }

    @Override
    @Cacheable(value = "product", key = "'org-active-' + #organizationId")
    public List<ProductDto> getActiveProducts(Long organizationId) {
        log.debug("Fetching active products for organization {}", organizationId);

        List<Product> products = productRepository.findByOrganizationIdAndIsActive(organizationId, true);
        return productMapper.toDtoList(products);
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", allEntries = true)
    public ProductDto createProduct(ProductDto dto) {
        log.info("Creating product for organization {}", dto.getOrganizationId());

        Product product = productMapper.toEntity(dto);
        Product saved = productRepository.save(product);

        log.info("Successfully created product with id {}", saved.getId());
        return productMapper.toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", allEntries = true)
    public ProductDto updateProduct(Long id, ProductDto dto) {
        log.info("Updating product with id {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        productMapper.updateEntity(dto, product);
        Product updated = productRepository.save(product);

        log.info("Successfully updated product with id {}", id);
        return productMapper.toDto(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product with id {}", id);

        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
        log.info("Successfully deleted product with id {}", id);
    }

    @Override
    public List<PromotionDto> getPromotions(Long organizationId) {
        log.debug("Fetching promotions for organization {}", organizationId);

        List<Promotion> promotions = promotionRepository.findByOrganizationId(organizationId);
        return promotionMapper.toDtoList(promotions);
    }

    @Override
    @Cacheable(value = "promotion", key = "'org-active-' + #organizationId")
    public List<PromotionDto> getActivePromotions(Long organizationId) {
        log.debug("Fetching active promotions for organization {}", organizationId);

        List<Promotion> promotions = promotionRepository.findActivePromotions(organizationId, LocalDate.now());
        return promotionMapper.toDtoList(promotions);
    }

    @Override
    @Transactional
    @CacheEvict(value = "promotion", allEntries = true)
    public PromotionDto createPromotion(PromotionDto dto) {
        log.info("Creating promotion for organization {}", dto.getOrganizationId());

        Promotion promotion = promotionMapper.toEntity(dto);
        Promotion saved = promotionRepository.save(promotion);

        log.info("Successfully created promotion with id {}", saved.getId());
        return promotionMapper.toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "promotion", allEntries = true)
    public PromotionDto updatePromotion(Long id, PromotionDto dto) {
        log.info("Updating promotion with id {}", id);

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        promotionMapper.updateEntity(dto, promotion);
        Promotion updated = promotionRepository.save(promotion);

        log.info("Successfully updated promotion with id {}", id);
        return promotionMapper.toDto(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "promotion", allEntries = true)
    public void deletePromotion(Long id) {
        log.info("Deleting promotion with id {}", id);

        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion not found with id: " + id);
        }

        promotionRepository.deleteById(id);
        log.info("Successfully deleted promotion with id {}", id);
    }
}

