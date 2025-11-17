package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.requests.*;
import com.example.TerraFund.services.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:4200"
        },
        allowCredentials = "true"
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        return authService.register(registerRequest, response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return authService.login(loginRequest, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        return authService.logout(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser() {
        return authService.me();
    }

    @PostMapping("/choose-role")
    public ResponseEntity<?> chooseRole(@RequestBody ChooseRoleRequest request) {
        return authService.chooseRole(request);
    }

    @PreAuthorize("hasRole('INVESTOR')")
    @PostMapping("/account-info/investor")
    public ResponseEntity<?> investorProfile(@RequestBody InvestorProfileRequest request){
        System.out.println("DID WE REACH THE CONTROLLER?");
        return authService.createInvestorProfile(request);
    }

    @PreAuthorize("hasRole('LAND_OWNER')")
    @PostMapping("/account-info/land-owner")
    public ResponseEntity<?> landOwnerProfile(@RequestBody LandOwnerProfileRequest request){
        return authService.createLandOwnerProfile(request);
    }
}
