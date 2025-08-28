package com.kmkm.clientdome.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class MockAadhaarController {

    @PostMapping("/mock-extract")
    public ResponseEntity<Map<String, Object>> extractAadhaar(@RequestParam("file") MultipartFile file) {
        // Mock response for Aadhaar extraction
        System.out.println("Mock Aadhaar extraction called for file: " + file.getOriginalFilename());
        
        Map<String, Object> mockResponse = Map.of(
            "status", "success",
            "data", Map.of(
                "name", "John Doe",
                "aadhaar_number", "1234-5678-9012",
                "address", "123 Main Street, City, State, PIN",
                "date_of_birth", "01/01/1990",
                "gender", "Male"
            ),
            "message", "Aadhaar data extracted successfully"
        );
        
        return ResponseEntity.ok(mockResponse);
    }
}
