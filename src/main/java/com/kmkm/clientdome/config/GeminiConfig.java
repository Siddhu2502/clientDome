package com.kmkm.clientdome.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Bean
    public Client geminiClient() {
        // This pattern is taken directly from page 2 of your documentation.
        // It correctly builds the client for the pure Gemini API.
        return Client.builder().apiKey(apiKey).build();
    }
}