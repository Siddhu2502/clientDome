package com.kmkm.clientdome.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final Path fileStorageLocation = Paths.get("temp_uploads").toAbsolutePath().normalize();
    
    public FileStorageService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String userId, String docType){
        String uniqueFilename = userId + "_" + file + "_" + docType + UUID.randomUUID().toString();

        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.contains("..")){
                throw new RuntimeException("File name has invalide sequence / no path: " + originalFilename);
            }

            Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return targetLocation.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file to the path" + file.getOriginalFilename() + "Try again / Contact ADMIN");
        }
    }
}
