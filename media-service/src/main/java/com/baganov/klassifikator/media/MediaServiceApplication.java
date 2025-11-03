/**
 * @file: MediaServiceApplication.java
 * @description: Main application class for Media Service
 * @dependencies: Spring Boot
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.baganov.klassifikator.media",
        "com.baganov.klassifikator.common"
})
@EntityScan("com.baganov.klassifikator.common.model.entity")
@EnableJpaRepositories("com.baganov.klassifikator.media.repository")
public class MediaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }
}

