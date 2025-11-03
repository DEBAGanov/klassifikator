/**
 * @file: ContentServiceApplication.java
 * @description: Main application class for Content Service
 * @dependencies: Spring Boot
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.baganov.klassifikator.content",
        "com.baganov.klassifikator.common"
})
@EntityScan("com.baganov.klassifikator.common.model.entity")
@EnableJpaRepositories("com.baganov.klassifikator.content.repository")
public class ContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }
}

