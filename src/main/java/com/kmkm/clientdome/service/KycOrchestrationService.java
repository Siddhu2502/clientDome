package com.kmkm.clientdome.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class KycOrchestrationService {
    private final WebClient webClient;
    private final DescopeTokenService tokenService;
    private static final String AADHAAR_DOME_URL = "http://localhost:8012/extract";

    public KycOrchestrationService(WebClient.Builder webClientBuilder, DescopeTokenService tokenService) {
        this.webClient = webClientBuilder.build();
        this.tokenService = tokenService;
    }

    public Mono<String> processAadhaar(String filePath) {
        // === THIS IS THE CRITICAL MODIFICATION ===
        // 1. Read the reliable MIME type from the companion file we saved.
        String mimeType = "application/octet-stream"; // A safe default
        try {
            Path mimeTypePath = Path.of(filePath + ".mimetype");
            if (Files.exists(mimeTypePath)) {
                mimeType = Files.readString(mimeTypePath);
                System.out.println("Read stored MIME type: " + mimeType);
            }
        } catch (IOException e) {
            System.err.println("Could not read MIME type file for " + filePath + ", using default.");
            // Log the exception but continue with the default
            e.printStackTrace();
        }

        // 2. Prepare the multipart request body
        FileSystemResource resource = new FileSystemResource(Path.of(filePath));
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("mimeType", mimeType); // Add the reliable MIME type as a form field
        // === END MODIFICATION ===

        return tokenService.getAccessToken("doc:extract:aadhaar")
                .flatMap(token -> {
                    System.out.println("Making authenticated call to aadharDome with manually fetched token...");
                    return webClient.post()
                            .uri(AADHAAR_DOME_URL)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .body(BodyInserters.fromMultipartData(body))
                            .retrieve()
                            .bodyToMono(String.class);
                });
    }
}