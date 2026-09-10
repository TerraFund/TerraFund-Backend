package com.example.TerraFund.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Service
public class FileUploadService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "png", "jpg", "jpeg", "gif", "webp",
            "doc", "docx", "xls", "xlsx", "txt"
    );

    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

    public FileUploadService() throws IOException {

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds the 10 MB size limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        String baseName = Paths.get(originalFilename).getFileName().toString();
        String extension = baseName.contains(".")
                ? baseName.substring(baseName.lastIndexOf('.') + 1).toLowerCase()
                : "";

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed: '" + extension + "'");
        }

        String filename = System.currentTimeMillis() + "_" + baseName;
        Path filePath = uploadDir.resolve(filename).normalize();

        if (!filePath.startsWith(uploadDir)) {
            throw new IllegalArgumentException("Invalid filename");
        }

        Files.copy(file.getInputStream(), filePath);
        return filename;
    }

    public File getFile(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }

        Path resolved = uploadDir.resolve(filename).normalize();
        if (!resolved.startsWith(uploadDir)) {
            throw new IllegalArgumentException("Invalid filename");
        }

        return resolved.toFile();
    }

    public Path getUploadDir() {
        return uploadDir;
    }
}