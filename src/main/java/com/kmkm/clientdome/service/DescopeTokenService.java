package com.kmkm.clientdome.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class DescopeTokenService {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUri;

    // A simple record to deserialize the JSON response from Descope
    private record DescopeTokenResponse(@JsonProperty("access_token") String accessToken) {}

    public DescopeTokenService(
            WebClient.Builder webClientBuilder,
            @Value("${spring.security.oauth2.client.registration.descope-m2m.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.descope-m2m.client-secret}") String clientSecret,
            @Value("${spring.security.oauth2.client.provider.descope-provider.token-uri}") String tokenUri) {
        
        this.webClient = webClientBuilder.build(); // Create a simple WebClient
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUri = tokenUri;
    }

    public Mono<String> getAccessToken(String scope) {
        System.out.println("==================== FETCHING DESCOPE M2M TOKEN ====================");
        System.out.println("Token URI: " + this.tokenUri);
        System.out.println("Client ID: " + this.clientId);
        System.out.println("Scope: " + scope);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("scope", scope);

        return this.webClient.post()
                .uri(this.tokenUri)
                .headers(headers -> headers.setBasicAuth(this.clientId, this.clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromValue(formData))
                .retrieve()
                .bodyToMono(DescopeTokenResponse.class)
                .map(DescopeTokenResponse::accessToken)
                .doOnSuccess(token -> {
                    System.out.println("Successfully retrieved token (first 10 chars): " + (token != null ? token.substring(0, 10) : "null") + "...");
                    System.out.println("====================================================================");
                })
                .doOnError(error -> {
                    System.err.println("!!! FAILED to retrieve token: " + error.getMessage());
                    System.err.println("====================================================================");
                });
    }
}