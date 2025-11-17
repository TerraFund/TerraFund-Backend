package com.example.TerraFund.controllers;

import com.example.TerraFund.entities.User;
import com.example.TerraFund.entities.Land;
import com.example.TerraFund.repositories.UserRepository;
import com.example.TerraFund.services.LandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "2. Admin Portal", description = "Admin-related endpoints")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:4200"}, allowCredentials = "true")
public class AdminController {

    private final UserRepository userRepository;
    private final LandService landService;

    @Operation(summary = "Get all users", tags = {"2. Admin Portal"})
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @Operation(summary = "Get all lands", tags = {"2. Admin Portal"})
    @GetMapping("/lands")
    public ResponseEntity<List<Land>> getAllLands() {
        return ResponseEntity.ok(landService.findAll());
    }

    @Operation(summary = "Flag land listing", tags = {"2. Admin Portal"})
    @PatchMapping("/flag/listing/{id}")
    public ResponseEntity<Land> flagLandListing(@PathVariable Long id) {
        return ResponseEntity.ok(landService.hideListing(id));
    }

    @Operation(summary = "Verify land listing", tags = {"2. Admin Portal"})
    @PatchMapping("/verify/land/{id}")
    public ResponseEntity<Land> verifyLand(@PathVariable Long id) {
        return ResponseEntity.ok(landService.verifyLand(id));
    }
}

