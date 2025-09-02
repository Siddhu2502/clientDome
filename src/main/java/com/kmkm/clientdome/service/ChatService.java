package com.kmkm.clientdome.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final Client geminiClient;
    private final String modelName;

    public record ChatTurn(String botResponseText, List<Content> updatedHistory) {}

    public ChatService(Client geminiClient, @Value("${gemini.model.name}") String modelName) {
        this.geminiClient = geminiClient;
        this.modelName = modelName;
    }

    public ChatTurn generateResponse(String userInput, List<Content> currentHistory, Map<String, String> uploadedFiles) {
        try {
            List<Content> conversationHistory = new ArrayList<>(currentHistory);

            // Add the new user message to the history
            conversationHistory.add(
                Content.builder()
                    .role("user")
                    // === FIX: Wrap the Part in a List using List.of() and use setParts ===
                    .parts(List.of(Part.builder().text(userInput).build()))
                    .build()
            );

            GenerateContentResponse response = this.geminiClient.models
                    .generateContent(this.modelName, conversationHistory, null);
            
            String botResponseText = response.text();

            // Add the model's response to the history as well
            String fullUserInput = buildStatefulUserInput(userInput, uploadedFiles);
            conversationHistory.add(
                Content.builder()
                    .role("model")
                    .parts(List.of(Part.builder().text(fullUserInput).build()))
                    .build()
            );
            
            return new ChatTurn(botResponseText, conversationHistory);

        } catch (Exception e) {
            e.printStackTrace();
            String errorText = "An error occurred while communicating with the AI service.";
            return new ChatTurn(errorText, currentHistory);
        }
    }


    private String buildStatefulUserInput(String userInput, Map<String, String> uploadedFiles) {
        StringBuilder contentsReport = new StringBuilder();

        contentsReport.append("--- SYSTEM CONTEXT ---\n");
        contentsReport.append("Current Document Upload Status:\n");

        // these all are required (right now we will just ask aadhar)
        // List<String> requiredDocs = List.of("aadhar", "pan", "marksheet", "photo");
        List<String> requiredDocs = List.of("aadhar");

        for (String doc: requiredDocs){
            String uploadStatus = uploadedFiles.containsKey(doc) ? "Uploaded" : "Pending";
            contentsReport.append(String.format("current uploaded does %s is %s", doc, uploadStatus));
        }

        long uploadCount = requiredDocs.stream().filter(uploadedFiles::containsKey).count();
        if (uploadCount == requiredDocs.size()){
            contentsReport.append("All documents are uploaded. Instruct the user to review and click the 'Process & Generate ID' button.\n");
        }
        contentsReport.append("--- END OF SYSTEM CONTEXT ---");
        contentsReport.append(userInput);


        return contentsReport.toString();
    }


    public List<Content> getInitialHistory() {
        String systemInstruction = """
            You are the "KMKM Guide", a friendly, helpful, and professional AI assistant for the Know-Me-Know-Me (KMKM) identity verification application.
            Your primary goal is to guide users through the KYC (Know Your Customer) process. The process requires uploading three documents: Aadhaar card, PAN card, and 10th-grade marksheet.
            Your tone should be encouraging and clear. Do not go off-topic.
            Start the very first conversation by greeting the user and explaining the process.
            """;
            
        return new ArrayList<>(List.of(
            Content.builder()
                .role("user")
                .parts(List.of(Part.builder().text(systemInstruction).build()))
                .build(),
            Content.builder()
                .role("model")
                .parts(List.of(Part.builder().text("Understood. I am the KMKM Guide. I will greet the user and assist them with the KYC process.").build()))
                .build()
        ));
    }
}