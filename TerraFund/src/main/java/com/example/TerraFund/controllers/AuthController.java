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
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Signup user", tags = {"1. Authentication"}, operationId = "01")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        return authService.register(registerRequest, response);
    }

    @Operation(summary = "Verify user", tags = {"1. Authentication"}, operationId = "02")
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest verifyRequest, HttpServletResponse response) {
        return authService.verify(verifyRequest, response);
    }

    @Operation(summary = "Login user", tags = {"1. Authentication"}, operationId = "03")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return authService.login(loginRequest, response);
    }

    @Operation(summary = "Refresh token", tags = {"1. Authentication"}, operationId = "04")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @Operation(summary = "Logout user", tags = {"1. Authentication"}, operationId = "05")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        return authService.logout(response);
    }

    @Operation(summary = "Forgot password", tags = {"1. Authentication"}, operationId = "06")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @Operation(summary = "Reset password", tags = {"1. Authentication"}, operationId = "07")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @Operation(summary = "Choose role", tags = {"1. Authentication"}, operationId = "08")
    @PostMapping("/choose-role")
    public ResponseEntity<?> chooseRole(@RequestBody ChooseRoleRequest request) {
        return authService.chooseRole(request);
    }

    @Operation(summary = "Complete investor's profile", tags = {"1. Authentication"}, operationId = "09")
    @PreAuthorize("hasRole('INVESTOR')")
    @PostMapping("/account-info/investor")
    public ResponseEntity<?> investorProfile(@RequestBody InvestorProfileRequest request){
        return authService.createInvestorProfile(request);
    }

    @Operation(summary = "Complete land owner's profile", tags = {"1. Authentication"}, operationId = "10")
    @PreAuthorize("hasRole('LAND_OWNER')")
    @PostMapping("/account-info/land-owner")
    public ResponseEntity<?> landOwnerProfile(@RequestBody LandOwnerProfileRequest request){
        return authService.createLandOwnerProfile(request);
    }

    @Operation(summary = "Update user by email", tags = {"1. Authentication"}, operationId = "11")
    @PutMapping("/account-info/land-owner/update")
    public ResponseEntity<?> updateAccountInfo(@RequestBody LandOwnerProfileRequest request){
        return authService.updateLandOwnerProfile(request);
    }

    @Operation(summary = "Update user by email", tags = {"1. Authentication"}, operationId = "12")
    @PutMapping("/account-info/investor/update")
    public ResponseEntity<?> updateInvestorAccountInfo(@RequestBody InvestorProfileRequest request){
        return authService.updateInvestorProfile(request);
    }

    @Operation(summary = "Get authenticated user", tags = {"1. Authentication"}, operationId = "13")
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser() {
        return authService.me();
    }
}
