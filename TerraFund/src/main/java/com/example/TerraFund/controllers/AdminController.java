package com.example.TerraFund.controllers;

import com.example.TerraFund.entities.User;
import com.example.TerraFund.model.Land;
import com.example.TerraFund.repositories.UserRepository;
import com.example.TerraFund.services.LandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:4200"}, allowCredentials = "true")
public class AdminController {

    private final UserRepository userRepository;
    private final LandService landService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/lands")
    public ResponseEntity<List<Land>> getAllLands() {
        return ResponseEntity.ok(landService.findAll());
    }

    @PatchMapping("/flag/listing/{id}")
    public ResponseEntity<Land> flagLandListing(@PathVariable Long id) {
        return ResponseEntity.ok(landService.hideListing(id));
    }

    @PatchMapping("/verify/land/{id}")
    public ResponseEntity<Land> verifyLand(@PathVariable Long id) {
        return ResponseEntity.ok(landService.verifyLand(id));
    }
}

