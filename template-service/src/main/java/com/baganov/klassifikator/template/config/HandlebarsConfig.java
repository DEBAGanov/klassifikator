/**
 * @file: HandlebarsConfig.java
 * @description: Configuration for Handlebars template engine
 * @dependencies: Handlebars
 * @created: 2025-11-02
 */
package com.baganov.klassifikator.template.config;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Configuration
public class HandlebarsConfig {

    @Bean
    public Handlebars handlebars() {
        Handlebars handlebars = new Handlebars();
        
        // Register custom helpers
        registerHelpers(handlebars);
        
        return handlebars;
    }

    private void registerHelpers(Handlebars handlebars) {
        // Helper for formatting prices
        handlebars.registerHelper("formatPrice", new Helper<Object>() {
            @Override
            public Object apply(Object context, Options options) throws IOException {
                if (context == null) {
                    return "";
                }
                
                try {
                    double price = Double.parseDouble(context.toString());
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("ru", "RU"));
                    return formatter.format(price);
                } catch (NumberFormatException e) {
                    return context.toString();
                }
            }
        });

        // Helper for formatting dates
        handlebars.registerHelper("formatDate", new Helper<Object>() {
            @Override
            public Object apply(Object context, Options options) throws IOException {
                if (context == null) {
                    return "";
                }
                
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    if (context instanceof Date) {
                        return formatter.format((Date) context);
                    } else if (context instanceof String) {
                        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse((String) context);
                        return formatter.format(date);
                    }
                    return context.toString();
                } catch (Exception e) {
                    return context.toString();
                }
            }
        });

        // Helper for conditional rendering
        handlebars.registerHelper("ifCond", new Helper<Object>() {
            @Override
            public Object apply(Object context, Options options) throws IOException {
                Object v1 = context;
                Object v2 = options.param(0);
                String operator = options.param(1, "==");
                
                boolean result = false;
                
                switch (operator) {
                    case "==":
                        result = v1 != null && v1.equals(v2);
                        break;
                    case "!=":
                        result = v1 == null || !v1.equals(v2);
                        break;
                    case ">":
                        result = compareNumbers(v1, v2) > 0;
                        break;
                    case "<":
                        result = compareNumbers(v1, v2) < 0;
                        break;
                    case ">=":
                        result = compareNumbers(v1, v2) >= 0;
                        break;
                    case "<=":
                        result = compareNumbers(v1, v2) <= 0;
                        break;
                }
                
                return result ? options.fn() : options.inverse();
            }
        });

        // Helper for string truncation
        handlebars.registerHelper("truncate", new Helper<String>() {
            @Override
            public Object apply(String context, Options options) throws IOException {
                if (context == null) {
                    return "";
                }
                
                int length = options.param(0, 100);
                if (context.length() <= length) {
                    return context;
                }
                
                return context.substring(0, length) + "...";
            }
        });
        
        // Helper for mapping feature names to icons
        handlebars.registerHelper("getFeatureIcon", new Helper<String>() {
            @Override
            public Object apply(String context, Options options) throws IOException {
                if (context == null) {
                    return "✓";
                }
                
                String featureName = context.toLowerCase().trim();
                
                // Map feature names to emoji icons
                if (featureName.contains("доставка")) {
                    return "🍕";
                } else if (featureName.contains("кофе")) {
                    return "☕";
                } else if (featureName.contains("навынос") || featureName.contains("на вынос")) {
                    return "🥡";
                } else if (featureName.contains("парковка")) {
                    return "🅿️";
                } else if (featureName.contains("завтрак")) {
                    return "⭐";
                } else if (featureName.contains("детск")) {
                    return "⭐";
                } else if (featureName.contains("вызов") || featureName.contains("персонал")) {
                    return "⭐";
                } else if (featureName.contains("предзаказ") || featureName.contains("онлайн")) {
                    return "⭐";
                } else {
                    return "✓";
                }
            }
        });
    }

    private int compareNumbers(Object v1, Object v2) {
        try {
            double d1 = Double.parseDouble(v1.toString());
            double d2 = Double.parseDouble(v2.toString());
            return Double.compare(d1, d2);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

