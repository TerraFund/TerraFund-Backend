package com.example.TerraFund.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

    /**
     * SECURITY fixes:
     * - Filenames from the client are never used: files are stored under a random UUID name.
     *   This removes the path-traversal write vulnerability ("../../" in originalFilename).
     * - Extension whitelist rejects executable/HTML uploads (stored XSS / code execution).
     * - Size limit enforced server-side.
     * - getFile() rejects path separators and verifies the resolved path stays inside
     *   the upload directory, removing the arbitrary-file-read vulnerability.
     */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

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
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds the 5MB size limit");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed: " + ALLOWED_EXTENSIONS);
        }

        // Client-controlled name is discarded; a random name prevents traversal/overwrites.
        String safeName = UUID.randomUUID() + "." + extension;
        Path filePath = uploadDir.resolve(safeName).normalize();
        if (!filePath.startsWith(uploadDir)) {
            throw new IOException("Invalid target path");
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return safeName;
    }

    public File getFile(String filename) {
        if (filename == null || filename.isBlank()
                || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        Path filePath = uploadDir.resolve(filename).normalize();
        if (!filePath.startsWith(uploadDir)) {
            throw new IllegalArgumentException("Invalid filename");
        }
        return filePath.toFile();
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase();
        // Reject double extensions like "file.jpg.html"
        return ext;
    }
}
