package com.example.TerraFund.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileUploadService {

    private final Path uploadDir = Paths.get("uploads");

    public FileUploadService() throws IOException {
        
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    public String saveFile(MultipartFile file) throws IOException {
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        return filename;
    }

    public File getFile(String filename) {
        return uploadDir.resolve(filename).toFile();
    }
}
