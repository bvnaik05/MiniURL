package com.example.miniURL.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads .env file before Spring Boot starts
 * This makes environment variables available from .env file
 * 
 * How it works:
 * 1. Loads .env file from project root
 * 2. Adds all variables to Spring Environment
 * 3. Spring Boot can now use ${DATABASE_URL} etc.
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // Load .env file from project root
            // App runs from: backend/miniURL, .env is at: ../../.env
            Dotenv dotenv = Dotenv.configure()
                    .directory("../../")  // Navigate to project root
                    .ignoreIfMissing()    // Don't fail if .env is missing (use H2 fallback)
                    .load();

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> envMap = new HashMap<>();
            
            // Add all .env variables to Spring Environment
            dotenv.entries().forEach(entry -> {
                envMap.put(entry.getKey(), entry.getValue());
                System.out.println("✅ Loaded: " + entry.getKey() + " = " + 
                    (entry.getKey().contains("PASSWORD") ? "***" : entry.getValue()));
            });

            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envMap));
            
            System.out.println("\n🎉 Successfully loaded .env file with " + envMap.size() + " variables!\n");
            
        } catch (Exception e) {
            System.out.println("\n⚠️  No .env file found - using H2 fallback for development\n");
            System.out.println("Error: " + e.getMessage());
        }
    }
}
