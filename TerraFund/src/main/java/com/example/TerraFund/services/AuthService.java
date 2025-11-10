package com.example.TerraFund.services;

import com.example.TerraFund.dto.RegisterRequest;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.UserRepository;
import com.example.TerraFund.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {
    private UserRepository userRepository;
    private JwtService jwtService;
    private final AuthenticationProvider authenticationProvider;

    public ResponseEntity<?> register(RegisterRequest registerRequest){
        if(!userRepository.existsByEmail("registerRequest.getEmail")){
            throw new RuntimeException("Email already exists!");
        }

        try{
            User user = new User();

            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());
            user.setRole(registerRequest.getRole());
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setPhoneNumber(registerRequest.getPhoneNumber());

            userRepository.save(user);

            return ResponseEntity.ok().body(jwtService.generateToken(user.getEmail()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
