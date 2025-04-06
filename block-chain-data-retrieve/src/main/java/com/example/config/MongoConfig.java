package com.example.config;

import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration class for MongoDB.
 * This ensures that MongoTemplate is properly configured for custom queries.
 */
@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    /**
     * Configure MongoTemplate with the correct database.
     * This bean is used for complex MongoDB queries.
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        // Create a new MongoTemplate with the MongoDB client and database name
        return new MongoTemplate(mongoClient, databaseName);
    }
} 