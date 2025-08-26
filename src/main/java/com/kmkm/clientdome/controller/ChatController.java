package com.kmkm.clientdome.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    
    public record ChatRequest(String message) {}

    @PostMapping("/api/chat")
    public ResponseEntity<?> handleChat(Principal principal, @RequestBody ChatRequest chatRequest){
        String userId = principal.getName();
        String userMessage = chatRequest.message();

        System.out.printf("Received message from user '%s': %s%n", userId, userMessage);

        String botResponse = "you have told this words -> " + userMessage;

        return ResponseEntity.ok(Map.of("response", botResponse));

    }
}
