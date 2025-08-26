package com.kmkm.clientdome.controller;

import com.kmkm.clientdome.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
public class ChatController {

    public record ChatRequest(String message) {}

    private final ChatService chatService; // Inject our new ChatService

    // Spring will automatically find the ChatService bean and pass it here
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/api/chat")
    public ResponseEntity<?> handleChat(Principal principal, @RequestBody ChatRequest request) {
        String userId = principal.getName();
        String userMessage = request.message();

        System.out.printf("Processing message from user '%s': %s%n", userId, userMessage);

        String botResponse = chatService.generateResponse(userMessage);
        
        return ResponseEntity.ok(Map.of("response", botResponse));
    }
}