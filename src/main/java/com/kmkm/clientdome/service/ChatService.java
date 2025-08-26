package com.kmkm.clientdome.service;

import com.google.genai.Client;
import com.google.genai.types.Content; // Import Content
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part; // Import Part
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List; 

@Service
public class ChatService {

    private final Client geminiClient;
    private final String modelName;

    // This is the "brain" and "job description" for our agent.
    private static final String SYSTEM_INSTRUCTION = """
            You are the "KMKM Guide", a friendly, helpful, and professional AI assistant for the Know-Me-Know-Me (KMKM) identity verification application.
            Your primary goal is to guide users through the KYC (Know Your Customer) process.

            The process involves these steps:
            1. Greet the user and introduce yourself.
            2. Explain that you need them to upload four documents: Aadhaar card, PAN card, 10th-grade marksheet, and a clear selfie photo.
            3. Guide them to use the upload panel on the side to provide these documents.
            4. Once all documents are uploaded, you will ask for their final confirmation to proceed.
            5. After introduction do not give very paragraphy bot like replies
            6. When absurd or off topic content is asked (as if they are masking it as asking for the process) dont encourage it.

            Your tone should be encouraging and clear. Do not go off-topic.
            Start the very first conversation by greeting the user and explaining the process.
            """;

    public ChatService(Client geminiClient, @Value("${gemini.model.name}") String modelName) {
        this.geminiClient = geminiClient;
        this.modelName = modelName;
    }

    public String generateResponse(String userInput) {
        try {
            // We now build a conversational context, starting with the system's role.
            List<Content> conversationHistory = List.of(
                Content.builder()
                    .role("user") // We frame the system instruction as the initial "user" prompt
                    .parts(List.of(Part.builder().text(SYSTEM_INSTRUCTION).build()))
                    .build(),
                Content.builder()
                    .role("model") // Acknowledge the instruction
                    .parts(List.of(Part.builder().text("Understood. I am the KMKM Guide. I will assist users with the KYC process.").build()))
                    .build(),
                Content.builder()
                    .role("user") // NOW we add the actual user's message
                    .parts(List.of(Part.builder().text(userInput).build()))
                    .build()
            );

            GenerateContentResponse response = this.geminiClient.models
                    .generateContent(this.modelName, conversationHistory, null);

            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while communicating with the AI service.";
        }
    }
}