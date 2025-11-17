package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.requests.CreateLandRequest;
import com.example.TerraFund.entities.Land;
import com.example.TerraFund.services.LandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Tag(name = "3. Land Portal", description = "Land-related endpoints")
@RequestMapping("/api/land")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:4200"}, allowCredentials = "true")
public class LandController {

    private final LandService landService;

    @Operation(summary = "Create land", tags = {"3. Land Portal"})
    @PreAuthorize("hasRole('LAND_OWNER')")
    @PostMapping("/create")
    public ResponseEntity<Land> createLand(@RequestBody CreateLandRequest createLandRequest) {
        Land saved = landService.create(createLandRequest);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Upload documents", tags = {"3. Land Portal"})
    @PreAuthorize("hasRole('LAND_OWNER')")
    @PostMapping("/upload-documents/{landId}")
    public ResponseEntity<String> uploadDocs(@PathVariable Long landId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok("Document uploaded successfully for land ID " + landId);
    }

    @Operation(summary = "Get my lands", tags = {"3. Land Portal"})
    @PreAuthorize("hasRole('LAND_OWNER')")
    @GetMapping("/my-lands/{ownerId}")
    public ResponseEntity<List<Land>> myLands(@PathVariable Long ownerId) {
        List<Land> lands = landService.findByOwner(ownerId);
        return ResponseEntity.ok(lands);
    }

    @Operation(summary = "Update land", tags = {"3. Land Portal"})
    @PreAuthorize("hasRole('LAND_OWNER')")
    @PatchMapping("/update/{id}")
    public ResponseEntity<Land> update(@PathVariable Long id, @RequestBody Land updatedLand) {
        updatedLand.setId(id);
        Land saved = landService.update(updatedLand);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Delete land", tags = {"3. Land Portal"})
    @PreAuthorize("hasRole('LAND_OWNER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        landService.delete(id);
        return ResponseEntity.ok("Land deleted successfully");
    }

    @Operation(summary = "Publish land", tags = {"3. Land Portal"})
    @PreAuthorize("hasRole('LAND_OWNER')")
    @PatchMapping("/publish/{id}")
    public ResponseEntity<String> publish(@PathVariable Long id) {
        
        return ResponseEntity.ok("Land published successfully (ID: " + id + ")");
    }

    @Operation(summary = "List published lands", tags = {"3. Land Portal"})
    @PreAuthorize("hasRole('LAND_OWNER')")
    @GetMapping("/list")
    public ResponseEntity<List<Land>> list() {
        List<Land> published = landService.listPublished();
        return ResponseEntity.ok(published);
    }

    @Operation(summary = "Get land details", tags = {"3. Land Portal"})
    @PreAuthorize("hasRole('LAND_OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<Land> landDetails(@PathVariable Long id) {
        return landService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get lands by owner", tags = {"3. Land Portal"})
    @PreAuthorize("hasRole('LAND_OWNER')")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Land>> landsByOwner(@PathVariable Long ownerId) {
        List<Land> lands = landService.findByOwner(ownerId);
        return ResponseEntity.ok(lands);
    }
}
