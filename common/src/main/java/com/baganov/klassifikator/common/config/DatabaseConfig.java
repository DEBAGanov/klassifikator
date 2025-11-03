/**
 * @file: DatabaseConfig.java
 * @description: Database configuration for PostgreSQL and JPA
 * @dependencies: Spring Data JPA, Flyway
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.baganov.klassifikator.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    // Additional database configuration if needed
}

