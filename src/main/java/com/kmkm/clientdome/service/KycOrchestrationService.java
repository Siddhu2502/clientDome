package com.kmkm.clientdome.service;

import org.springframework.core.ParameterizedTypeReference;
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
import java.util.Map;

@Service
public class KycOrchestrationService {
    private final WebClient webClient;
    private final DescopeTokenService tokenService;

    // Define the URLs for ALL our specialist agents
    private static final String AADHAAR_DOME_URL = "http://localhost:8012/extract";
    private static final String PAN_DOME_URL = "http://localhost:8013/extract";
    private static final String MARKSHEET_DOME_URL = "http://localhost:8014/extract";
    private static final String VALIDATOR_DOME_URL= "http://localhost:8016/validate";

    public KycOrchestrationService(WebClient.Builder webClientBuilder, DescopeTokenService tokenService) {
        this.webClient = webClientBuilder.build();
        this.tokenService = tokenService;
    }

    // === THE NEW, REUSABLE GENERIC METHOD ===
    /**
     * A generic method to call any specialist document extraction agent.
     * It handles MIME type reading, body creation, token fetching, and the API
     * call.
     * 
     * @param filePath The path to the file to be uploaded.
     * @param agentUrl The URL of the specialist agent to call.
     * @param scope    The specific OAuth2 scope required for this operation.
     * @return A Mono containing the string response from the specialist agent.
     */
    private Mono<Map<String, String>> callSpecialistAgent(String filePath, String agentUrl, String scope) {
        // 1. Read the reliable MIME type from the companion file.
        String mimeType = "application/octet-stream"; // A safe default
        try {
            Path mimeTypePath = Path.of(filePath + ".mimetype");
            if (Files.exists(mimeTypePath)) {
                mimeType = Files.readString(mimeTypePath);
            }
        } catch (IOException e) {
            System.err.println("Could not read MIME type file for " + filePath + ", using default.");
        }

        // 2. Prepare the multipart request body.
        FileSystemResource resource = new FileSystemResource(Path.of(filePath));
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("mimeType", mimeType);

        // 3. Get the token and make the authenticated call.
        return tokenService.getAccessToken(scope)
                .flatMap(token -> {
                    System.out.printf("Making authenticated call to [%s] with scope [%s]...%n", agentUrl, scope);
                    return webClient.post()
                            .uri(agentUrl)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .body(BodyInserters.fromMultipartData(body))
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                            });
                });
    }

    public Mono<Map<String, Object>> validateKycData(Map<String, String> extractedData) {
        return tokenService.getAccessToken("kyc:validate")
                .flatMap(token -> {
                    System.out.printf("Making final validation call to [%s] with scope [kyc:validate]...%n",
                            VALIDATOR_DOME_URL);
                    return webClient.post()
                            .uri(VALIDATOR_DOME_URL)
                            .contentType(MediaType.APPLICATION_JSON) // The content type is JSON
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .body(BodyInserters.fromValue(extractedData)) // Send the map as the JSON body
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                            }); // Expect a Map back
                });
    }

    public Mono<Map<String, String>> processAadhaar(String filePath) {
        return callSpecialistAgent(filePath, AADHAAR_DOME_URL, "doc:extract:aadhaar");
    }

    public Mono<Map<String, String>> processPan(String filePath) {
        return callSpecialistAgent(filePath, PAN_DOME_URL, "doc:extract:pan");
    }

    public Mono<Map<String, String>> processMarksheet(String filePath) {
        return callSpecialistAgent(filePath, MARKSHEET_DOME_URL, "doc:extract:marksheet");
    }
}