/**
 * @file: OrderApplication.java
 * @description: Main application class for Order Service
 * @dependencies: Spring Boot, JPA, Redis
 * @created: 2025-11-03
 */
package com.baganov.klassifikator.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.baganov.klassifikator.order",
        "com.baganov.klassifikator.common"
})
@EnableJpaRepositories(basePackages = {
        "com.baganov.klassifikator.order.repository",
        "com.baganov.klassifikator.common.repository"
})
@EntityScan(basePackages = {
        "com.baganov.klassifikator.common.model.entity"
})
@EnableCaching
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}

