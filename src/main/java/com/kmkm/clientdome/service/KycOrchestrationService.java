package com.kmkm.clientdome.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

import reactor.core.publisher.Mono;

@Service
public class KycOrchestrationService {
    private final WebClient webClient;
    // Temporarily using mock endpoint for testing
    private static final String AADHAR_DOME_URL = "http://localhost:8011/mock-extract";

    public KycOrchestrationService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> processAadhar(String filePath) {
        FileSystemResource resource = new FileSystemResource(filePath);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        return this.webClient.post()
                .uri(AADHAR_DOME_URL)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                // Removed OAuth2 authentication for mock endpoint
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(String.class);
    }
}
