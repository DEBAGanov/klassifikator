/**
 * @file: TemplateServiceApplication.java
 * @description: Main application class for Template Service
 * @dependencies: Spring Boot
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.baganov.klassifikator.template",
        "com.baganov.klassifikator.common"
})
@EntityScan("com.baganov.klassifikator.common.model.entity")
@EnableJpaRepositories("com.baganov.klassifikator.template.repository")
public class TemplateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateServiceApplication.class, args);
    }
}

