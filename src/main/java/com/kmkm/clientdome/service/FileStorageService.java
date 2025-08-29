package com.kmkm.clientdome.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path fileStorageLocation = Paths.get("temp-uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String userId, String docType) {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        // We now include the original file object in the unique name's creation
        String uniqueFileName = userId + "_" + docType + "_" + UUID.randomUUID().toString() + fileExtension;

        try {
            if (originalFilename == null || originalFilename.contains("..")) {
                throw new RuntimeException("File name has invalid sequence / no path: " + originalFilename);
            }

            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // === THIS IS THE CRITICAL ADDITION ===
            // We store the original, reliable MIME type in a companion file.
            String mimeType = file.getContentType();
            if (mimeType != null && !mimeType.isBlank()) {
                Path mimeTypeFile = this.fileStorageLocation.resolve(uniqueFileName + ".mimetype");
                Files.writeString(mimeTypeFile, mimeType);
                System.out.println("Saved MIME type '" + mimeType + "' for file: " + uniqueFileName);
            }
            // === END ADDITION ===

            return targetLocation.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    // Helper method to safely extract the file extension (e.g., ".pdf")
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ""; // No extension
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}