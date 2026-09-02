package com.example.TerraFund.controllers;

import com.example.TerraFund.entities.Land;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "2. Admin Portal", description = "Admin-related endpoints")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get all users", tags = {"2. Admin Portal"})
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.findAllUsers());
    }

    @Operation(summary = "Get all lands", tags = {"2. Admin Portal"})
    @GetMapping("/lands")
    public ResponseEntity<List<Land>> getAllLands() {
        return ResponseEntity.ok(adminService.findAllLands());
    }

    @Operation(summary = "Flag or hide land listing", tags = {"2. Admin Portal"})
    @PatchMapping("/flag/listing/{id}")
    public ResponseEntity<Land> flagLandListing(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.hideListing(id));
    }

    @Operation(summary = "Unhide land listing", tags = {"2. Admin Portal"})
    @PatchMapping("/unhide/listing/{id}")
    public ResponseEntity<Land> unhideLandListing(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.unhideListing(id));
    }

    @Operation(summary = "Verify land listing", tags = {"2. Admin Portal"})
    @PatchMapping("/verify/land/{id}")
    public ResponseEntity<Land> verifyLand(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.verifyLand(id));
    }

    @Operation(summary = "Unverify land listing", tags = {"2. Admin Portal"})
    @PatchMapping("/unverify/land/{id}")
    public ResponseEntity<Land> unverifyLand(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.unverifyLand(id));
    }
}
