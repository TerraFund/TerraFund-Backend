package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.RegisterRequest;
import com.example.TerraFund.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private RegisterRequest registerRequest;
    private AuthService authService;

    public ResponseEntity<?> register(RegisterRequest registerRequest){
        return authService.register(registerRequest);
    }
}
