package com.example.TerraFund.controllers;

import com.example.TerraFund.services.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filename = fileUploadService.saveFile(file);
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("success", true);
            resp.put("message", "File uploaded successfully: " + filename);
            resp.put("filename", filename);
            resp.put("url", "/api/files/download/" + filename);
            return ResponseEntity.ok(resp);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("success", false, "error", "Failed to upload file"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
        try {
            File file = fileUploadService.getFile(filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] content = Files.readAllBytes(file.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", file.getName());

            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
