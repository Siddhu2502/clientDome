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

import java.nio.file.Path;

@Service
public class KycOrchestrationService {
    private final WebClient webClient;
    private final DescopeTokenService tokenService; // We must use our manual token service
    private static final String AADHAAR_DOME_URL = "http://localhost:8012/extract";

    public KycOrchestrationService(WebClient.Builder webClientBuilder, DescopeTokenService tokenService) {
        this.webClient = webClientBuilder.build();
        this.tokenService = tokenService;
    }

    public Mono<String> processAadhaar(String filePath) {
        FileSystemResource resource = new FileSystemResource(Path.of(filePath));
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

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