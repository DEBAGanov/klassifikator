/**
 * @file: GoogleSheetsDataProcessor.java
 * @description: Service for processing and mapping Google Sheets data to entities
 * @dependencies: Spring, WebClient
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value("${landing-service.url:http://localhost:8081}")
    private String landingServiceUrl;

    /**
     * Process all organizations from Google Sheets and create/update them automatically
     */
    public void processAllOrganizationsFromSheet(String spreadsheetId, String sheetName) {
        log.info("Processing all organizations from sheet {}", sheetName);
        
        try {
            // TODO: Read all rows from Google Sheets
            // For now, this method will be called from GoogleSheetsServiceImpl
            // which will pass the data
            log.info("Auto-discovery not yet implemented. Use syncOrganizationData() with organizationId");
        } catch (Exception e) {
            log.error("Failed to process organizations from sheet", e);
            throw new RuntimeException("Failed to process organizations: " + e.getMessage());
        }
    }

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
     * Helper method to get string value with fallback for trailing spaces
     * Tries field name with and without trailing space
     */
    private String getStringValueWithFallback(Map<String, Object> data, String key, String defaultValue) {
        // Try both with and without trailing space
        String value = getStringValue(data, key, key + " ", key.trim());
        if (!value.isEmpty()) {
            log.debug("Found value for key '{}': {}", key, value.length() > 50 ? value.substring(0, 50) + "..." : value);
            return value;
        }
        
        log.debug("No value found for key '{}', using default: '{}'", key, defaultValue);
        return defaultValue;
    }
    
    /**
     * Update organization content
     */
    private void updateOrganizationContent(Long organizationId, Map<String, Object> data) {
        log.debug("Updating content for organization {}", organizationId);

        // Create additional content data
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("phone", getStringValueWithFallback(data, "Телефон", ""));
        additionalData.put("email", getStringValueWithFallback(data, "Email", ""));
        additionalData.put("address", getStringValueWithFallback(data, "Адрес", ""));
        additionalData.put("workingHours", getStringValueWithFallback(data, "Режим работы", ""));
        additionalData.put("organizationName", getStringValueWithFallback(data, "Название", ""));
        additionalData.put("category", getStringValueWithFallback(data, "Категория", ""));
        additionalData.put("type", getStringValueWithFallback(data, "Тип", ""));
        additionalData.put("domain", getStringValueWithFallback(data, "Домен", ""));
        
        // Yandex Reviews Widget (HTML код виджета)
        String reviewsWidget = getStringValueWithFallback(data, "Отзывы", "");
        if (!reviewsWidget.isEmpty()) {
            additionalData.put("yandexReviewsWidget", reviewsWidget);
        }
        
        // Yandex Map Widget (HTML код виджета карты)
        String mapWidget = getStringValueWithFallback(data, "Карта", "");
        if (!mapWidget.isEmpty()) {
            additionalData.put("yandexMapWidget", mapWidget);
        }
        
        // Company Photos for Hero Slider (ссылки на изображения для слайдера)
        String companyPhotosStr = getStringValueWithFallback(data, "Фото компании", "");
        if (!companyPhotosStr.isEmpty()) {
            List<String> companyPhotosList = Arrays.stream(companyPhotosStr.split("[,;\\n]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            additionalData.put("companyPhotos", companyPhotosList);
        }
        
        // Photos (ссылки на изображения для фотогалереи)
        String photosStr = getStringValueWithFallback(data, "Фото", "");
        if (!photosStr.isEmpty()) {
            // Если это URL на папку S3 или список URL через запятую
            List<String> photosList = Arrays.stream(photosStr.split("[,;\\n]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            additionalData.put("photos", photosList);
        }
        
        // Features (особенности организации, разделенные запятой)
        String featuresStr = getStringValueWithFallback(data, "Особенности", "");
        if (!featuresStr.isEmpty()) {
            List<String> featuresList = Arrays.stream(featuresStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            additionalData.put("features", featuresList);
        }

        // Create ContentDto matching the expected structure
        Map<String, Object> contentDto = new HashMap<>();
        contentDto.put("organizationId", organizationId);
        contentDto.put("title", getStringValueWithFallback(data, "Title", getStringValueWithFallback(data, "Название", "")));
        contentDto.put("metaDescription", getStringValueWithFallback(data, "Description", ""));
        contentDto.put("h1", getStringValueWithFallback(data, "H1", getStringValueWithFallback(data, "Название", "")));
        
        // About Us text
        String aboutUsText = getStringValueWithFallback(data, "О нас", "");
        contentDto.put("aboutText", aboutUsText);
        
        // Also add to additional data for template rendering
        if (!aboutUsText.isEmpty()) {
            additionalData.put("aboutUs", aboutUsText);
        }
        
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
     * Replaces ALL existing products with new ones from Google Sheets
     */
    private void updateProducts(Long organizationId, List<Map<String, Object>> products) {
        log.info("Replacing products for organization {} (deleting old, creating {} new)", organizationId, products.size());

        WebClient webClient = webClientBuilder.baseUrl(contentServiceUrl).build();

        // Step 1: Delete ALL existing products for this organization
        try {
            log.debug("Fetching existing products for organization {}", organizationId);
            String existingProductsJson = webClient.get()
                    .uri("/api/v1/content/organization/{organizationId}/products", organizationId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse and delete each existing product
            List<Long> existingProductIds = extractAllProductIds(existingProductsJson);
            log.info("Found {} existing products to delete", existingProductIds.size());
            
            for (Long productId : existingProductIds) {
                try {
                    log.debug("Deleting product with id: {}", productId);
                    webClient.delete()
                            .uri("/api/v1/content/products/{id}", productId)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .block();
                } catch (Exception e) {
                    log.warn("Failed to delete product with id: {}", productId, e);
                }
            }
            
            log.info("Successfully deleted {} old products", existingProductIds.size());
        } catch (Exception e) {
            log.warn("Failed to delete existing products: {}", e.getMessage());
        }

        // Step 2: Create ALL new products from Google Sheets
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

                // Create new product
                log.debug("Creating new product: {}", productName);
                webClient.post()
                        .uri("/api/v1/content/products")
                        .bodyValue(product)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                log.debug("Created product: {}", productName);
            } catch (Exception e) {
                log.error("Failed to create product: {}", product.get("name"), e);
            }
        }

        log.info("Successfully replaced products for organization {} ({} new products created)", organizationId, products.size());
    }
    
    /**
     * Extract all product IDs from JSON response
     */
    private List<Long> extractAllProductIds(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return new ArrayList<>();
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            List<Long> productIds = new ArrayList<>();
            
            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("id")) {
                        productIds.add(node.get("id").asLong());
                    }
                }
            }
            
            return productIds;
        } catch (Exception e) {
            log.error("Failed to parse products JSON", e);
            return new ArrayList<>();
        }
    }

    /**
     * Update promotions via Content Service
     * Replaces ALL existing promotions with new ones from Google Sheets
     */
    private void updatePromotions(Long organizationId, List<Map<String, Object>> promotions) {
        log.info("Replacing promotions for organization {} (deleting old, creating {} new)", organizationId, promotions.size());

        WebClient webClient = webClientBuilder.baseUrl(contentServiceUrl).build();

        // Step 1: Delete ALL existing promotions for this organization
        try {
            log.debug("Fetching existing promotions for organization {}", organizationId);
            String existingPromotionsJson = webClient.get()
                    .uri("/api/v1/content/organization/{organizationId}/promotions", organizationId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse and delete each existing promotion
            List<Long> existingPromotionIds = extractAllPromotionIds(existingPromotionsJson);
            log.info("Found {} existing promotions to delete", existingPromotionIds.size());
            
            for (Long promotionId : existingPromotionIds) {
                try {
                    log.debug("Deleting promotion with id: {}", promotionId);
                    webClient.delete()
                            .uri("/api/v1/content/promotions/{id}", promotionId)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .block();
                } catch (Exception e) {
                    log.warn("Failed to delete promotion with id: {}", promotionId, e);
                }
            }
            
            log.info("Successfully deleted {} old promotions", existingPromotionIds.size());
        } catch (Exception e) {
            log.warn("Failed to delete existing promotions: {}", e.getMessage());
        }

        // Step 2: Create ALL new promotions from Google Sheets
        for (Map<String, Object> promotion : promotions) {
            try {
                promotion.put("organizationId", organizationId);
                String promotionTitle = promotion.get("title").toString();

                // Create new promotion
                log.debug("Creating new promotion: {}", promotionTitle);
                webClient.post()
                        .uri("/api/v1/content/promotions")
                        .bodyValue(promotion)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                log.debug("Created promotion: {}", promotionTitle);
            } catch (Exception e) {
                log.error("Failed to create promotion: {}", promotion.get("title"), e);
            }
        }

        log.info("Successfully replaced promotions for organization {} ({} new promotions created)", organizationId, promotions.size());
    }
    
    /**
     * Extract all promotion IDs from JSON response
     */
    private List<Long> extractAllPromotionIds(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return new ArrayList<>();
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            List<Long> promotionIds = new ArrayList<>();
            
            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("id")) {
                        promotionIds.add(node.get("id").asLong());
                    }
                }
            }
            
            return promotionIds;
        } catch (Exception e) {
            log.error("Failed to parse promotions JSON", e);
            return new ArrayList<>();
        }
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
     * Process a single organization row from Google Sheets (backward compatibility)
     * @deprecated Use {@link #processOrganizationRow(Map, String, String, List, List)} instead
     */
    @Deprecated
    public boolean processOrganizationRow(Map<String, Object> row, String domain, String name) {
        return processOrganizationRow(row, domain, name, new ArrayList<>(), new ArrayList<>());
    }
    
    /**
     * Process a single organization row from Google Sheets
     * Creates Organization + Landing + Content if they don't exist
     * Updates Content if organization exists
     * 
     * @param row Data from Google Sheets row
     * @param domain Domain name (e.g., "modernissimo.volzhck.ru")
     * @param name Organization name
     * @param productsRows List of product rows for this domain
     * @param promotionsRows List of promotion rows for this domain
     * @return true if new organization was created, false if updated
     */
    public boolean processOrganizationRow(Map<String, Object> row, String domain, String name, 
                                          List<Map<String, Object>> productsRows, 
                                          List<Map<String, Object>> promotionsRows) {
        log.info("Processing organization: {} ({})", name, domain);
        
        try {
            WebClient webClient = webClientBuilder.baseUrl(landingServiceUrl).build();
            
            // Check if Landing with this domain or subdomain already exists
            String subdomain = domain.split("\\.")[0]; // Extract subdomain part
            
            String checkByDomainResponse = webClient.get()
                    .uri("/api/v1/landings/domain/{domain}", domain)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.just(""))
                    .block();
            
            String checkBySubdomainResponse = webClient.get()
                    .uri("/api/v1/landings/by-subdomain/{subdomain}", subdomain)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.just(""))
                    .block();
            
            boolean landingExists = (checkByDomainResponse != null && !checkByDomainResponse.isEmpty() && !checkByDomainResponse.contains("404")) ||
                                   (checkBySubdomainResponse != null && !checkBySubdomainResponse.isEmpty() && !checkBySubdomainResponse.contains("404"));
            
            Long organizationId;
            Long landingId;
            
            if (!landingExists) {
                // CREATE NEW ORGANIZATION + LANDING
                log.info("Creating new organization and landing for: {}", name);
                
                // 1. Create Organization
                Map<String, Object> organizationData = new HashMap<>();
                organizationData.put("name", name);
                organizationData.put("category", getStringValueWithFallback(row, "Категория", "Общее"));
                organizationData.put("type", getStringValueWithFallback(row, "Тип", "Компания"));
                organizationData.put("phone", getStringValueWithFallback(row, "Телефон", ""));
                organizationData.put("email", getStringValueWithFallback(row, "Email", ""));
                organizationData.put("website", getStringValueWithFallback(row, "Сайт", ""));
                organizationData.put("address", getStringValueWithFallback(row, "Адрес", ""));
                organizationData.put("workingHours", getStringValueWithFallback(row, "Режим работы", ""));
                
                // Telegram bot for organization (optional)
                organizationData.put("telegramBotToken", getStringValueWithFallback(row, "Токен бота", ""));
                organizationData.put("telegramChatId", getStringValueWithFallback(row, "Chat ID", ""));
                
                organizationData.put("status", "ACTIVE");
                
                String orgResponse = webClient.post()
                        .uri("/api/v1/organizations")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .bodyValue(organizationData)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                organizationId = extractIdFromJson(orgResponse);
                log.info("✅ Created organization with ID: {}", organizationId);
                
                // 2. Create Landing
                Map<String, Object> landingData = new HashMap<>();
                landingData.put("organizationId", organizationId);
                landingData.put("domain", domain);
                landingData.put("subdomain", domain.split("\\.")[0]); // Extract subdomain part
                landingData.put("templateId", 1L); // Always use template ID = 1 (landing-basic)
                landingData.put("title", getStringValueWithFallback(row, "Title", name));
                landingData.put("metaDescription", getStringValueWithFallback(row, "Description", ""));
                landingData.put("status", "ACTIVE");
                
                String landingResponse = webClient.post()
                        .uri("/api/v1/landings")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .bodyValue(landingData)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                landingId = extractIdFromJson(landingResponse);
                log.info("✅ Created landing with ID: {} for domain: {}", landingId, domain);
                
                // 3. Create Content from row data
                List<Map<String, Object>> dataAsList = new ArrayList<>();
                dataAsList.add(row);
                processAndUpdateOrganization(organizationId, dataAsList);
                
                // 4. Process products from separate sheet (ALWAYS call to ensure deletion of old products)
                log.info("Processing {} products for organization {}", productsRows.size(), organizationId);
                updateProducts(organizationId, parseProductsFromSeparateSheet(productsRows));
                
                // 5. Process promotions from separate sheet (ALWAYS call to ensure deletion of old promotions)
                log.info("Processing {} promotions for organization {}", promotionsRows.size(), organizationId);
                updatePromotions(organizationId, parsePromotionsFromSeparateSheet(promotionsRows));
                
                return true; // New organization created
                
            } else {
                // UPDATE EXISTING ORGANIZATION
                log.info("Updating existing organization for domain: {}", domain);
                
                // Extract organizationId from landing response (use whichever check succeeded)
                String landingResponse = (checkByDomainResponse != null && !checkByDomainResponse.isEmpty() && !checkByDomainResponse.contains("404")) 
                        ? checkByDomainResponse 
                        : checkBySubdomainResponse;
                organizationId = extractOrganizationIdFromLandingJson(landingResponse);
                
                if (organizationId != null) {
                    // Update content
                    List<Map<String, Object>> dataAsList = new ArrayList<>();
                    dataAsList.add(row);
                    processAndUpdateOrganization(organizationId, dataAsList);
                    
                    // Update products from separate sheet (ALWAYS call to ensure deletion if no products in sheet)
                    log.info("Updating {} products for organization {}", productsRows.size(), organizationId);
                    updateProducts(organizationId, parseProductsFromSeparateSheet(productsRows));
                    
                    // Update promotions from separate sheet (ALWAYS call to ensure deletion if no promotions in sheet)
                    log.info("Updating {} promotions for organization {}", promotionsRows.size(), organizationId);
                    updatePromotions(organizationId, parsePromotionsFromSeparateSheet(promotionsRows));
                    
                    return false; // Existing organization updated
                } else {
                    log.warn("Could not extract organizationId from landing response");
                    return false;
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to process organization row for: {}", name, e);
            throw new RuntimeException("Failed to process organization: " + e.getMessage());
        }
    }
    
    /**
     * Delete organizations and landings that are NOT in the Google Sheets
     * @param sheetDomains Set of domains from Google Sheets
     * @return Number of organizations deleted
     */
    public int deleteOrganizationsNotInSheet(Set<String> sheetDomains) {
        log.info("Deleting organizations not in Google Sheets. Valid domains: {}", sheetDomains);
        
        WebClient landingClient = webClientBuilder.baseUrl(landingServiceUrl).build();
        int deletedCount = 0;
        
        try {
            // 1. Get all landings
            String allLandingsJson = landingClient.get()
                    .uri("/api/v1/landings")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (allLandingsJson == null || allLandingsJson.isEmpty() || "[]".equals(allLandingsJson.trim())) {
                log.info("No landings found in database");
                return 0;
            }
            
            // 2. Parse landings and find those not in Google Sheets
            // Simple JSON parsing to extract domains and IDs
            String[] landingBlocks = allLandingsJson.split("\\{");
            
            for (String block : landingBlocks) {
                if (!block.contains("\"domain\"")) continue;
                
                try {
                    // Extract domain from JSON block
                    String domain = null;
                    Long landingId = null;
                    Long organizationId = null;
                    
                    // Find domain
                    int domainStart = block.indexOf("\"domain\":\"") + 10;
                    if (domainStart > 9) {
                        int domainEnd = block.indexOf("\"", domainStart);
                        if (domainEnd > domainStart) {
                            domain = block.substring(domainStart, domainEnd).trim();
                        }
                    }
                    
                    // Find landing ID
                    int idStart = block.indexOf("\"id\":") + 5;
                    if (idStart > 4) {
                        int idEnd = block.indexOf(",", idStart);
                        if (idEnd == -1) idEnd = block.indexOf("}", idStart);
                        if (idEnd > idStart) {
                            String idStr = block.substring(idStart, idEnd).trim();
                            landingId = Long.parseLong(idStr);
                        }
                    }
                    
                    // Find organization ID
                    int orgIdStart = block.indexOf("\"organizationId\":") + 17;
                    if (orgIdStart > 16) {
                        int orgIdEnd = block.indexOf(",", orgIdStart);
                        if (orgIdEnd == -1) orgIdEnd = block.indexOf("}", orgIdStart);
                        if (orgIdEnd > orgIdStart) {
                            String orgIdStr = block.substring(orgIdStart, orgIdEnd).trim();
                            organizationId = Long.parseLong(orgIdStr);
                        }
                    }
                    
                    // If this domain is NOT in Google Sheets, delete it
                    if (domain != null && landingId != null && organizationId != null) {
                        if (!sheetDomains.contains(domain)) {
                            log.info("🗑️  Deleting landing {} and organization {} for domain: {} (not in Google Sheets)", 
                                    landingId, organizationId, domain);
                            
                            // Delete landing first
                            try {
                                landingClient.delete()
                                        .uri("/api/v1/landings/{id}", landingId)
                                        .retrieve()
                                        .bodyToMono(Void.class)
                                        .block();
                                log.debug("Deleted landing {}", landingId);
                            } catch (Exception e) {
                                log.warn("Failed to delete landing {}: {}", landingId, e.getMessage());
                            }
                            
                            // Then delete organization
                            try {
                                landingClient.delete()
                                        .uri("/api/v1/organizations/{id}", organizationId)
                                        .retrieve()
                                        .bodyToMono(Void.class)
                                        .block();
                                log.debug("Deleted organization {}", organizationId);
                                deletedCount++;
                            } catch (Exception e) {
                                log.warn("Failed to delete organization {}: {}", organizationId, e.getMessage());
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    log.warn("Failed to parse landing block: {}", e.getMessage());
                }
            }
            
            log.info("Successfully deleted {} organizations not in Google Sheets", deletedCount);
            
        } catch (Exception e) {
            log.error("Failed to delete old organizations", e);
            throw new RuntimeException("Failed to delete old organizations: " + e.getMessage());
        }
        
        return deletedCount;
    }
    
    /**
     * Extract ID from JSON response
     */
    private Long extractIdFromJson(String jsonResponse) {
        try {
            // Simple JSON parsing to extract "id" field
            int idIndex = jsonResponse.indexOf("\"id\":");
            if (idIndex == -1) {
                return null;
            }
            
            String afterId = jsonResponse.substring(idIndex + 5).trim();
            int commaIndex = afterId.indexOf(",");
            String idStr = commaIndex > 0 ? afterId.substring(0, commaIndex) : afterId;
            idStr = idStr.trim().replaceAll("[^0-9]", "");
            
            return Long.parseLong(idStr);
        } catch (Exception e) {
            log.warn("Failed to extract ID from JSON", e);
            return null;
        }
    }
    
    /**
     * Extract organizationId from Landing JSON response
     */
    private Long extractOrganizationIdFromLandingJson(String jsonResponse) {
        try {
            int orgIdIndex = jsonResponse.indexOf("\"organizationId\":");
            if (orgIdIndex == -1) {
                return null;
            }
            
            String afterOrgId = jsonResponse.substring(orgIdIndex + 17).trim();
            int commaIndex = afterOrgId.indexOf(",");
            String orgIdStr = commaIndex > 0 ? afterOrgId.substring(0, commaIndex) : afterOrgId;
            orgIdStr = orgIdStr.trim().replaceAll("[^0-9]", "");
            
            return Long.parseLong(orgIdStr);
        } catch (Exception e) {
            log.warn("Failed to extract organizationId from JSON", e);
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
    
    /**
     * Parse products from separate "goods" sheet
     * Format: Домен | Товар | Описание товара | Цена | Категория товара | Изображение
     */
    private List<Map<String, Object>> parseProductsFromSeparateSheet(List<Map<String, Object>> productRows) {
        List<Map<String, Object>> products = new ArrayList<>();
        
        for (Map<String, Object> row : productRows) {
            String productName = getStringValueWithFallback(row, "Товар", "").trim();
            
            // Skip rows without product name
            if (productName.isEmpty()) {
                continue;
            }
            
            Map<String, Object> product = new HashMap<>();
            product.put("name", productName);
            product.put("description", getStringValueWithFallback(row, "Описание товара", ""));
            product.put("category", getStringValueWithFallback(row, "Категория товара", ""));
            product.put("imageUrl", getStringValueWithFallback(row, "Изображение", ""));
            
            // Parse price
            String priceStr = getStringValueWithFallback(row, "Цена", "0").trim();
            double price = 0.0;
            try {
                if (!priceStr.isEmpty()) {
                    // Remove all non-numeric characters except dot and comma
                    priceStr = priceStr.replaceAll("[^0-9.,]", "");
                    // Replace comma with dot for decimal separator
                    priceStr = priceStr.replace(",", ".");
                    price = Double.parseDouble(priceStr);
                }
                log.debug("Parsed price {} for product {}", price, productName);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse price for product {}: {}", productName, priceStr);
                price = 0.0;
            }
            product.put("price", price);
            
            products.add(product);
            log.debug("Parsed product: {}", productName);
        }
        
        return products;
    }
    
    /**
     * Parse promotions from separate "акции" sheet
     * Format: Домен | Акция | Описание акции | Действует до
     */
    private List<Map<String, Object>> parsePromotionsFromSeparateSheet(List<Map<String, Object>> promotionRows) {
        List<Map<String, Object>> promotions = new ArrayList<>();
        
        for (Map<String, Object> row : promotionRows) {
            String promotionName = getStringValueWithFallback(row, "Акция", "").trim();
            
            // Skip rows without promotion name
            if (promotionName.isEmpty()) {
                log.debug("Skipping promotion row with no name");
                continue;
            }
            
            Map<String, Object> promotion = new HashMap<>();
            promotion.put("title", promotionName);
            promotion.put("description", getStringValueWithFallback(row, "Описание акции", ""));
            String validUntil = getStringValueWithFallback(row, "Действует до", "");
            promotion.put("validUntil", validUntil);
            
            promotions.add(promotion);
            log.info("Parsed promotion: {} (until: {})", promotionName, validUntil);
        }
        
        log.info("Total promotions parsed: {}", promotions.size());
        return promotions;
    }
}

