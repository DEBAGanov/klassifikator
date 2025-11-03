/**
 * @file: AppProperties.java
 * @description: Application configuration properties
 * @dependencies: Spring Boot Configuration
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Domain domain = new Domain();
    private Google google = new Google();
    private S3 s3 = new S3();
    private Telegram telegram = new Telegram();
    private Yandex yandex = new Yandex();

    @Data
    public static class Domain {
        private String base;
    }

    @Data
    public static class Google {
        private Sheets sheets = new Sheets();

        @Data
        public static class Sheets {
            private String applicationName;
            private String credentialsFilePath;
        }
    }

    @Data
    public static class S3 {
        private String endpoint;
        private String region;
        private String accessKey;
        private String secretKey;
        private String bucketName;
    }

    @Data
    public static class Telegram {
        private String botToken;
        private String chatId;
    }

    @Data
    public static class Yandex {
        private String mapsApiKey;
        private String metrikaCounterId;
    }
}

