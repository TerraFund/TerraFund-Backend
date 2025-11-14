package com.example.TerraFund.Utils;



import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Component
public class FileStorageUtil {

    private final Path root = Paths.get("uploads");

    public FileStorageUtil() throws IOException {
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
    }

    public String saveFile(MultipartFile file) throws IOException {
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path destination = root.resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }
}
