package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.requests.LandRequest;
import com.example.TerraFund.entities.Land;
import com.example.TerraFund.services.LandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/land")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:4200"}, allowCredentials = "true")
public class LandController {

    private final LandService landService;

    @PreAuthorize("hasRole('LAND_OWNER')")
    @PostMapping("/create")
    public ResponseEntity<Land> createLand(@RequestBody LandRequest landRequest) {
        Land saved = landService.create(landRequest);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('LAND_OWNER')")
    @PostMapping("/upload-documents/{landId}")
    public ResponseEntity<String> uploadDocs(@PathVariable Long landId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok("Document uploaded successfully for land ID " + landId);
    }

    @PreAuthorize("hasRole('LAND_OWNER')")
    @GetMapping("/my-lands/{ownerId}")
    public ResponseEntity<List<Land>> myLands(@PathVariable Long ownerId) {
        List<Land> lands = landService.findByOwner(ownerId);
        return ResponseEntity.ok(lands);
    }

    @PreAuthorize("hasRole('LAND_OWNER')")
    @PatchMapping("/update/{id}")
    public ResponseEntity<Land> update(@PathVariable Long id, @RequestBody Land updatedLand) {
        updatedLand.setId(id);
        Land saved = landService.update(updatedLand);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('LAND_OWNER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        landService.delete(id);
        return ResponseEntity.ok("Land deleted successfully");
    }

    @PreAuthorize("hasRole('LAND_OWNER')")
    @PatchMapping("/publish/{id}")
    public ResponseEntity<String> publish(@PathVariable Long id) {
        
        return ResponseEntity.ok("Land published successfully (ID: " + id + ")");
    }

    @PreAuthorize("hasRole('LAND_OWNER')")
    @GetMapping("/list")
    public ResponseEntity<List<Land>> list() {
        List<Land> published = landService.listPublished();
        return ResponseEntity.ok(published);
    }

    @PreAuthorize("hasRole('LAND_OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<Land> landDetails(@PathVariable Long id) {
        return landService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('LAND_OWNER')")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Land>> landsByOwner(@PathVariable Long ownerId) {
        List<Land> lands = landService.findByOwner(ownerId);
        return ResponseEntity.ok(lands);
    }
}
