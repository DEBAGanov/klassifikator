/**
 * @file: IntegrationServiceApplication.java
 * @description: Main application class for Integration Service
 * @dependencies: Spring Boot
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.baganov.klassifikator.integration",
        "com.baganov.klassifikator.common"
})
@EntityScan("com.baganov.klassifikator.common.model.entity")
@EnableJpaRepositories("com.baganov.klassifikator.integration.repository")
@EnableScheduling
public class IntegrationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationServiceApplication.class, args);
    }
}

