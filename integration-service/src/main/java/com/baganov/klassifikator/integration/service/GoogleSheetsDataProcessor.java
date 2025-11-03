/**
 * @file: GoogleSheetsDataProcessor.java
 * @description: Service for processing and mapping Google Sheets data to entities
 * @dependencies: Spring, WebClient
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.integration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetsDataProcessor {

    private final WebClient.Builder webClientBuilder;

    @Value("${content-service.url:http://localhost:8082}")
    private String contentServiceUrl;

    @Value("${media-service.url:http://localhost:8084}")
    private String mediaServiceUrl;

    /**
     * Process Google Sheets data and update organization
     */
    public void processAndUpdateOrganization(Long organizationId, List<Map<String, Object>> sheetData) {
        log.info("Processing {} rows for organization {}", sheetData.size(), organizationId);

        if (sheetData.isEmpty()) {
            log.warn("No data to process");
            return;
        }

        // Assuming first row contains organization data
        Map<String, Object> orgData = sheetData.get(0);

        // Update organization content
        updateOrganizationContent(organizationId, orgData);

        // Process products (if any)
        List<Map<String, Object>> products = extractProducts(sheetData);
        if (!products.isEmpty()) {
            updateProducts(organizationId, products);
        }

        // Process promotions (if any)
        List<Map<String, Object>> promotions = extractPromotions(sheetData);
        if (!promotions.isEmpty()) {
            updatePromotions(organizationId, promotions);
        }

        log.info("Successfully processed data for organization {}", organizationId);
    }

    /**
     * Update organization content
     */
    private void updateOrganizationContent(Long organizationId, Map<String, Object> data) {
        log.debug("Updating content for organization {}", organizationId);

        // Create additional content data
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("phone", getStringValue(data, "Телефон", ""));
        additionalData.put("email", getStringValue(data, "Email", ""));
        additionalData.put("address", getStringValue(data, "Адрес", ""));
        additionalData.put("workingHours", getStringValue(data, "Режим работы", ""));
        additionalData.put("organizationName", getStringValue(data, "Название", ""));
        additionalData.put("category", getStringValue(data, "Категория", ""));
        additionalData.put("type", getStringValue(data, "Тип", ""));
        additionalData.put("domain", getStringValue(data, "Домен", ""));

        // Create ContentDto matching the expected structure
        Map<String, Object> contentDto = new HashMap<>();
        contentDto.put("organizationId", organizationId);
        contentDto.put("title", getStringValue(data, "Title", "Название"));
        contentDto.put("metaDescription", getStringValue(data, "Description", "Описание"));
        contentDto.put("h1", getStringValue(data, "H1", "Название"));
        contentDto.put("aboutText", getStringValue(data, "О нас", ""));
        contentDto.put("contentData", additionalData);

        WebClient webClient = webClientBuilder.baseUrl(contentServiceUrl).build();

        try {
            webClient.post()
                    .uri("/api/v1/content")
                    .bodyValue(contentDto)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Successfully updated content for organization {}", organizationId);
        } catch (Exception e) {
            log.error("Failed to update content for organization {}", organizationId, e);
            throw new RuntimeException("Failed to update content: " + e.getMessage());
        }
    }

    /**
     * Extract products from sheet data
     */
    private List<Map<String, Object>> extractProducts(List<Map<String, Object>> sheetData) {
        // Look for rows that contain product data
        // Assuming products start from row 2 and have specific columns
        return sheetData.stream()
                .filter(row -> hasValue(row, "Товар", "Товар ", "Product"))
                .map(row -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("name", getStringValue(row, "Товар", "Товар ", "Product"));
                    product.put("description", getStringValue(row, "Описание товара", "Описание товара ", ""));
                    // Try multiple variants of "Цена" with/without spaces
                    String priceKey = hasValue(row, "Цена ") ? "Цена " : "Цена";
                    product.put("price", getPriceValue(row, priceKey, "0"));
                    product.put("category", getStringValue(row, "Категория товара", "Категория", ""));
                    product.put("isActive", true);
                    
                    // Process image URL if provided
                    String imageUrl = getStringValue(row, "Изображение", "Изображение ", "Image");
                    if (!imageUrl.isEmpty()) {
                        product.put("imageUrl", imageUrl);
                    }
                    
                    return product;
                })
                .filter(product -> !product.get("name").toString().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Extract promotions from sheet data
     */
    private List<Map<String, Object>> extractPromotions(List<Map<String, Object>> sheetData) {
        return sheetData.stream()
                .filter(row -> hasValue(row, "Акция", "Акция ", "Promotion"))
                .map(row -> {
                    Map<String, Object> promotion = new HashMap<>();
                    promotion.put("title", getStringValue(row, "Акция", "Акция ", "Promotion"));
                    promotion.put("description", getStringValue(row, "Описание акции", ""));
                    promotion.put("validFrom", LocalDateTime.now());
                    // Try multiple variants of "Действует до" with/without spaces
                    String dateKey = hasValue(row, " Действует до") ? " Действует до" : "Действует до";
                    promotion.put("validTo", getDateValue(row, dateKey, LocalDateTime.now().plusMonths(1)));
                    promotion.put("isActive", true);
                    return promotion;
                })
                .filter(promo -> !promo.get("title").toString().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Update products via Content Service
     */
    private void updateProducts(Long organizationId, List<Map<String, Object>> products) {
        log.info("Updating {} products for organization {}", products.size(), organizationId);

        WebClient webClient = webClientBuilder.baseUrl(contentServiceUrl).build();

        for (Map<String, Object> product : products) {
            try {
                product.put("organizationId", organizationId);
                String productName = product.get("name").toString();

                // Process image URL if provided
                if (product.containsKey("imageUrl")) {
                    String imageUrl = product.get("imageUrl").toString();
                    if (!imageUrl.isEmpty()) {
                        log.debug("Processing image for product: {}", productName);
                        try {
                            // Upload image to S3 via Media Service
                            Long imageId = uploadImageFromUrl(imageUrl, organizationId, productName);
                            if (imageId != null) {
                                product.put("imageId", imageId);
                                product.remove("imageUrl"); // Remove URL, use imageId instead
                                log.debug("Image uploaded successfully for product: {} (imageId: {})", productName, imageId);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to upload image for product: {}", productName, e);
                            product.remove("imageUrl"); // Remove failed URL
                        }
                    }
                }

                // Check if product already exists
                try {
                    String existingProductsJson = webClient.get()
                            .uri("/api/v1/content/organization/{organizationId}/products", organizationId)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    // Parse and find existing product by name
                    Long existingProductId = findProductIdByName(existingProductsJson, productName);
                    
                    if (existingProductId != null) {
                        // Update existing product
                        log.debug("Updating existing product: {} (id: {})", productName, existingProductId);
                        webClient.put()
                                .uri("/api/v1/content/products/{id}", existingProductId)
                                .bodyValue(product)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .block();
                    } else {
                        // Create new product
                        log.debug("Creating new product: {}", productName);
                        webClient.post()
                                .uri("/api/v1/content/products")
                                .bodyValue(product)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .block();
                    }
                } catch (Exception e) {
                    log.warn("Could not check existing products, creating new: {}", productName);
                    webClient.post()
                            .uri("/api/v1/content/products")
                            .bodyValue(product)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();
                }

                log.debug("Processed product: {}", productName);
            } catch (Exception e) {
                log.error("Failed to process product: {}", product.get("name"), e);
            }
        }

        log.info("Successfully updated products for organization {}", organizationId);
    }

    /**
     * Update promotions via Content Service
     */
    private void updatePromotions(Long organizationId, List<Map<String, Object>> promotions) {
        log.info("Updating {} promotions for organization {}", promotions.size(), organizationId);

        WebClient webClient = webClientBuilder.baseUrl(contentServiceUrl).build();

        for (Map<String, Object> promotion : promotions) {
            try {
                promotion.put("organizationId", organizationId);
                String promotionTitle = promotion.get("title").toString();

                // Check if promotion already exists
                try {
                    String existingPromotionsJson = webClient.get()
                            .uri("/api/v1/content/organization/{organizationId}/promotions", organizationId)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    // Parse and find existing promotion by title
                    Long existingPromotionId = findPromotionIdByTitle(existingPromotionsJson, promotionTitle);
                    
                    if (existingPromotionId != null) {
                        // Update existing promotion
                        log.debug("Updating existing promotion: {} (id: {})", promotionTitle, existingPromotionId);
                        webClient.put()
                                .uri("/api/v1/content/promotions/{id}", existingPromotionId)
                                .bodyValue(promotion)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .block();
                    } else {
                        // Create new promotion
                        log.debug("Creating new promotion: {}", promotionTitle);
                        webClient.post()
                                .uri("/api/v1/content/promotions")
                                .bodyValue(promotion)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .block();
                    }
                } catch (Exception e) {
                    log.warn("Could not check existing promotions, creating new: {}", promotionTitle);
                    webClient.post()
                            .uri("/api/v1/content/promotions")
                            .bodyValue(promotion)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();
                }

                log.debug("Processed promotion: {}", promotionTitle);
            } catch (Exception e) {
                log.error("Failed to process promotion: {}", promotion.get("title"), e);
            }
        }

        log.info("Successfully updated promotions for organization {}", organizationId);
    }

    /**
     * Get string value from row data
     */
    private String getStringValue(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            if (data.containsKey(key) && data.get(key) != null) {
                return data.get(key).toString().trim();
            }
        }
        return "";
    }

    /**
     * Get price value from row data
     */
    private BigDecimal getPriceValue(Map<String, Object> data, String key, String defaultValue) {
        try {
            String value = getStringValue(data, key);
            if (value.isEmpty()) {
                value = defaultValue;
            }
            // Remove currency symbols and spaces
            value = value.replaceAll("[^0-9.,]", "");
            value = value.replace(",", ".");
            return new BigDecimal(value);
        } catch (Exception e) {
            log.warn("Failed to parse price value: {}", data.get(key));
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get date value from row data
     */
    private LocalDateTime getDateValue(Map<String, Object> data, String key, LocalDateTime defaultValue) {
        try {
            String value = getStringValue(data, key);
            if (value.isEmpty()) {
                return defaultValue;
            }

            // Try different date formats
            DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.parse(value, formatter);
                } catch (Exception ignored) {
                }
            }

            return defaultValue;
        } catch (Exception e) {
            log.warn("Failed to parse date value: {}", data.get(key));
            return defaultValue;
        }
    }

    /**
     * Check if row has value for key
     */
    private boolean hasValue(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            if (data.containsKey(key) && data.get(key) != null && !data.get(key).toString().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Upload image from URL and return imageId
     */
    private Long uploadImageFromUrl(String imageUrl, Long organizationId, String productName) {
        log.debug("Uploading image from URL for product: {}", productName);

        try {
            WebClient webClient = webClientBuilder.baseUrl(mediaServiceUrl).build();

            Map<String, Object> request = new HashMap<>();
            request.put("organizationId", organizationId);
            request.put("imageUrl", imageUrl);
            request.put("imageName", productName.replaceAll("[^a-zA-Z0-9а-яА-Я]", "_"));

            Map<String, Object> response = webClient.post()
                    .uri("/api/v1/media/upload-from-url")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("fileId")) {
                return Long.valueOf(response.get("fileId").toString());
            }

            log.warn("No fileId in response from Media Service");
            return null;

        } catch (Exception e) {
            log.error("Failed to upload image from URL: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * Download and upload image to S3
     */
    public String processImage(String imageUrl, Long organizationId, String imageName) {
        log.info("Processing image {} for organization {}", imageName, organizationId);

        try {
            // TODO: Download image from URL
            // TODO: Upload to S3 via Media Service
            // TODO: Return S3 URL

            WebClient webClient = webClientBuilder.baseUrl(mediaServiceUrl).build();

            // Placeholder implementation
            Map<String, Object> request = new HashMap<>();
            request.put("organizationId", organizationId);
            request.put("imageUrl", imageUrl);
            request.put("imageName", imageName);

            Map<String, Object> response = webClient.post()
                    .uri("/api/v1/media/upload-from-url")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("url")) {
                return response.get("url").toString();
            }

            return imageUrl;

        } catch (Exception e) {
            log.error("Failed to process image: {}", imageName, e);
            return imageUrl;
        }
    }

    /**
     * Find product ID by name from JSON response
     */
    private Long findProductIdByName(String jsonResponse, String productName) {
        try {
            // Simple JSON parsing to find product by name
            if (jsonResponse == null || jsonResponse.isEmpty() || "[]".equals(jsonResponse.trim())) {
                return null;
            }
            
            // Parse JSON manually (simple approach)
            String searchPattern = "\"name\":\"" + productName.replace("\"", "\\\"") + "\"";
            int nameIndex = jsonResponse.indexOf(searchPattern);
            
            if (nameIndex == -1) {
                return null;
            }
            
            // Find the ID field before the name
            String beforeName = jsonResponse.substring(0, nameIndex);
            int idIndex = beforeName.lastIndexOf("\"id\":");
            
            if (idIndex == -1) {
                return null;
            }
            
            // Extract ID value
            String afterId = beforeName.substring(idIndex + 5).trim();
            int commaIndex = afterId.indexOf(",");
            String idStr = commaIndex > 0 ? afterId.substring(0, commaIndex) : afterId;
            idStr = idStr.trim().replaceAll("[^0-9]", "");
            
            return Long.parseLong(idStr);
        } catch (Exception e) {
            log.warn("Failed to parse product ID for name: {}", productName, e);
            return null;
        }
    }

    /**
     * Find promotion ID by title from JSON response
     */
    private Long findPromotionIdByTitle(String jsonResponse, String promotionTitle) {
        try {
            // Simple JSON parsing to find promotion by title
            if (jsonResponse == null || jsonResponse.isEmpty() || "[]".equals(jsonResponse.trim())) {
                return null;
            }
            
            // Parse JSON manually (simple approach)
            String searchPattern = "\"title\":\"" + promotionTitle.replace("\"", "\\\"") + "\"";
            int titleIndex = jsonResponse.indexOf(searchPattern);
            
            if (titleIndex == -1) {
                return null;
            }
            
            // Find the ID field before the title
            String beforeTitle = jsonResponse.substring(0, titleIndex);
            int idIndex = beforeTitle.lastIndexOf("\"id\":");
            
            if (idIndex == -1) {
                return null;
            }
            
            // Extract ID value
            String afterId = beforeTitle.substring(idIndex + 5).trim();
            int commaIndex = afterId.indexOf(",");
            String idStr = commaIndex > 0 ? afterId.substring(0, commaIndex) : afterId;
            idStr = idStr.trim().replaceAll("[^0-9]", "");
            
            return Long.parseLong(idStr);
        } catch (Exception e) {
            log.warn("Failed to parse promotion ID for title: {}", promotionTitle, e);
            return null;
        }
    }
}

