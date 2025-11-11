package com.example.TerraFund.services;

import com.example.TerraFund.dto.LoginRequest;
import com.example.TerraFund.dto.RegisterRequest;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.UserRepository;
import com.example.TerraFund.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {
    private UserRepository userRepository;
    private JwtService jwtService;
    private final AuthenticationProvider authenticationProvider;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<?> register(RegisterRequest registerRequest){
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new RuntimeException("Email already exists!");
        }

        if(registerRequest.getPassword().length() < 6){
            throw new RuntimeException("Password must be at least 6 characters long!");
        }

        if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())){
            throw new RuntimeException("Passwords do not match!");
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

    public ResponseEntity<?> login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String jwt = jwtService.generateToken(request.getEmail());
        return ResponseEntity.ok(jwt);
    }
}
