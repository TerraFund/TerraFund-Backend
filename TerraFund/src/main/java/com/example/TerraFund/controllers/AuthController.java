package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.requests.*;
import com.example.TerraFund.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor
@RestController
@Tag(name = "1. Authentication", description = "Auth-related endpoints")
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

    @Operation(summary = "Login user", tags = {"1. Authentication"})
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        return authService.register(registerRequest, response);
    }

    @Operation(summary = "Verify user", tags = {"1. Authentication"})
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest verifyRequest, HttpServletResponse response) {
        return authService.verify(verifyRequest, response);
    }

    @Operation(summary = "Login user", tags = {"1. Authentication"})
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return authService.login(loginRequest, response);
    }

    @Operation(summary = "Refresh token", tags = {"1. Authentication"})
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @Operation(summary = "Logout user", tags = {"1. Authentication"})
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        return authService.logout(response);
    }

    @Operation(summary = "Forgot password", tags = {"1. Authentication"})
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @Operation(summary = "Reset password", tags = {"1. Authentication"})
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @Operation(summary = "Get authenticated user", tags = {"1. Authentication"})
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser() {
        return authService.me();
    }

    @Operation(summary = "Choose role", tags = {"1. Authentication"})
    @PostMapping("/choose-role")
    public ResponseEntity<?> chooseRole(@RequestBody ChooseRoleRequest request) {
        return authService.chooseRole(request);
    }

    @Operation(summary = "Get user by email", tags = {"1. Authentication"})
    @PreAuthorize("hasRole('INVESTOR')")
    @PostMapping("/account-info/investor")
    public ResponseEntity<?> investorProfile(@RequestBody InvestorProfileRequest request){
        System.out.println("DID WE REACH THE CONTROLLER?");
        return authService.createInvestorProfile(request);
    }

    @Operation(summary = "Get user by email", tags = {"1. Authentication"})
    @PreAuthorize("hasRole('LAND_OWNER')")
    @PostMapping("/account-info/land-owner")
    public ResponseEntity<?> landOwnerProfile(@RequestBody LandOwnerProfileRequest request){
        return authService.createLandOwnerProfile(request);
    }

    @Operation(summary = "Update user by email", tags = {"1. Authentication"})
    @PutMapping("/account-info/land-owner/update")
    public ResponseEntity<?> updateAccountInfo(@RequestBody LandOwnerProfileRequest request){
        return authService.updateLandOwnerProfile(request);
    }

    @Operation(summary = "Update user by email", tags = {"1. Authentication"})
    @PutMapping("/account-info/investor/update")
    public ResponseEntity<?> updateInvestorAccountInfo(@RequestBody InvestorProfileRequest request){
        return authService.updateInvestorProfile(request);
    }
}
