package com.kmkm.clientdome.controller;

import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.kmkm.clientdome.dto.ChatMessage;
import com.kmkm.clientdome.service.ChatService;
import com.kmkm.clientdome.service.FileStorageService;
import com.kmkm.clientdome.service.KycOrchestrationService;

import jakarta.servlet.http.HttpSession;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    public record ChatRequest(String message) {
    }

    private final ChatService chatService;
    private final FileStorageService fileStorageService;

    private final KycOrchestrationService orchestrationService;

    public ChatController(ChatService chatService, FileStorageService fileStorageService,
            KycOrchestrationService orchestrationService) {
        this.chatService = chatService;
        this.fileStorageService = fileStorageService;
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/api/chat")
    public ResponseEntity<?> handleChat(Principal principal, @RequestBody ChatRequest request, HttpSession session) {
        String userId = principal.getName();
        String userMessage = request.message();
        System.out.printf("Processing message from user '%s': %s%n", userId, userMessage);

        List<ChatMessage> serializableHistory = (List<ChatMessage>) session.getAttribute("conversationHistory");

        List<Content> history;
        if (serializableHistory == null) {
            history = chatService.getInitialHistory();
        } else {
            history = serializableHistory.stream()
                    .map(msg -> Content.builder()
                            .role(msg.role())
                            .parts(List.of(Part.builder().text(msg.text()).build()))
                            .build())
                    .toList();
        }

        Map<String, String> uploadedFiles = (Map<String, String>) session.getAttribute("uploadedFiles");
        if (uploadedFiles == null) {
            uploadedFiles = new HashMap<>();
        }

        ChatService.ChatTurn chatTurn = chatService.generateResponse(userMessage, history, uploadedFiles);

        List<ChatMessage> updatedSerializableHistory = chatTurn.updatedHistory().stream()
                .map(content -> {
                    // 1. Safely unwrap the Optional<String> for the role. Default to "user" if
                    // absent.
                    String role = content.role().orElse("user");

                    // 2. Safely unwrap the Optional<List<Part>> and then get the text.
                    String text = content.parts()
                            .map(parts -> parts.isEmpty() ? "" : parts.get(0).text().orElse(""))
                            .orElse(""); // If the entire parts list is absent

                    return new ChatMessage(role, text);
                }).toList();

        session.setAttribute("conversationHistory", updatedSerializableHistory);

        return ResponseEntity.ok(Map.of("response", chatTurn.botResponseText()));
    }

    @PostMapping("/api/upload")
    public ResponseEntity<Map<String, Object>> handleFileUpload(Principal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") String docType,
            HttpSession session) {
        String userId = principal.getName();

        System.out.printf("Receiving file upload for user '%s', docType '%s'%n", userId, docType);

        // store the file (invoke the service)
        String filePath = fileStorageService.storeFile(file, userId, docType);

        Map<String, String> uploadedFiles = (Map<String, String>) session.getAttribute("uploadedFiles");
        if (uploadedFiles == null) {
            uploadedFiles = new HashMap<>();
        }
        uploadedFiles.put(docType, filePath);
        session.setAttribute("uploadedFiles", uploadedFiles);

        System.out.println("File stored at: " + filePath + ". Session updated.");

        return ResponseEntity.ok(Map.of("message", "File uploaded success", "docType", docType));
    }

    // globalized for adding more and more parallel agents
    @PostMapping("/api/process-kyc")
    public Mono<ResponseEntity<Map<String, Object>>> processKyc(HttpSession session) {
        System.out.println("FULL KYC ORCHESTRATION TRIGGERED");
        @SuppressWarnings("unchecked")
        Map<String, String> uploadedFiles = (Map<String, String>) session.getAttribute("uploadedFiles");

        if (uploadedFiles == null || !uploadedFiles.containsKey("aadhar") || !uploadedFiles.containsKey("pan") || !uploadedFiles.containsKey("marksheet")) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Required documents are missing.")));
        }

        String aadhaarPath = uploadedFiles.get("aadhar");
        String panPath = uploadedFiles.get("pan");
        String marksheetPath = uploadedFiles.get("marksheet");

        // STAGE 1: Run all extractor agents in parallel
        Mono<Map<String, String>> aadhaarResponseMono = orchestrationService.processAadhaar(aadhaarPath);
        Mono<Map<String, String>> panResponseMono = orchestrationService.processPan(panPath);
        Mono<Map<String, String>> marksheetResponseMono = orchestrationService.processMarksheet(marksheetPath);

        return Mono.zip(aadhaarResponseMono, panResponseMono, marksheetResponseMono)
            .flatMap(tuple -> {
                // This block executes after all extractors have successfully completed.
                Map<String, String> aadhaarResult = tuple.getT1();
                Map<String, String> panResult = tuple.getT2();
                Map<String, String> marksheetResult = tuple.getT3();

                // Extract only the core data string to pass to the validator
                String aadhaarData = aadhaarResult.getOrDefault("extractedData", "");
                String panData = panResult.getOrDefault("extractedData", "");
                String marksheetData = marksheetResult.getOrDefault("extractedData", "");

                // Create the payload for the validator agent
                Map<String, String> validationPayload = Map.of(
                    "aadhaarData", aadhaarData,
                    "panData", panData,
                    "marksheetData", marksheetData
                );
                
                // STAGE 2: Chain the result into the final validation call
                System.out.println("All extractions complete. Handing off to validatorDome...");
                return orchestrationService.validateKycData(validationPayload);
            })
            .map(finalResult -> {
                // This block executes after the validatorDome returns its verdict.
                return ResponseEntity.ok(finalResult);
            })
            .doOnError(e -> System.err.println("Error during full KYC orchestration: " + e.getMessage()))
            .onErrorReturn(
                ResponseEntity.status(500).body(Map.of("error", "A critical error occurred during KYC processing."))
            );
    }    
}