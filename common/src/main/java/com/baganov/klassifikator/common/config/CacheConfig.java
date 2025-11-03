/**
 * @file: CacheConfig.java
 * @description: Redis cache configuration
 * @dependencies: Spring Cache, Redis
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CONTENT_CACHE = "content";
    public static final String LANDING_CACHE = "landing";
    public static final String TEMPLATE_CACHE = "template";
    public static final String PRODUCT_CACHE = "product";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Create ObjectMapper with JSR310 (Java 8 Date/Time) support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofHours(1));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(CONTENT_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration(LANDING_CACHE, defaultConfig.entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration(TEMPLATE_CACHE, defaultConfig.entryTtl(Duration.ofHours(24)))
                .withCacheConfiguration(PRODUCT_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)))
                .build();
    }
}

