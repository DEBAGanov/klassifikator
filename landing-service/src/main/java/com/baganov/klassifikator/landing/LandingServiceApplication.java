/**
 * @file: LandingServiceApplication.java
 * @description: Main application class for Landing Service
 * @dependencies: Spring Boot
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.landing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.baganov.klassifikator.landing",
        "com.baganov.klassifikator.common"
})
@EntityScan("com.baganov.klassifikator.common.model.entity")
@EnableJpaRepositories("com.baganov.klassifikator.landing.repository")
public class LandingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LandingServiceApplication.class, args);
    }
}

