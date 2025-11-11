package com.example.TerraFund.services;

import com.example.TerraFund.dto.LoginRequest;
import com.example.TerraFund.dto.RegisterRequest;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.UserRepository;
import com.example.TerraFund.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Service
public class AuthService {
    private UserRepository userRepository;
    private JwtService jwtService;
    private final AuthenticationProvider authenticationProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> register(RegisterRequest registerRequest, HttpServletResponse response){
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists!");
        }

        if(registerRequest.getPassword().length() < 6){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters long!");
        }

        if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match!");
        }

        try{
            User user = new User();

            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(registerRequest.getRole());
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setPhoneNumber(registerRequest.getPhoneNumber());

            userRepository.save(user);

            String accessToken = jwtService.generateAccessToken(registerRequest.getEmail(), registerRequest.getRole(), user.getId());
            String refreshToken = jwtService.generateRefreshToken(registerRequest.getEmail(), registerRequest.getRole(), user.getId());

            var cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(604800);
            cookie.setSecure(true);
            response.addCookie(cookie);

            return ResponseEntity.ok(accessToken);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> login(LoginRequest request, HttpServletResponse response){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        String accessToken = jwtService.generateAccessToken(request.getEmail(), user.getRole(), user.getId());
        String refreshToken = jwtService.generateRefreshToken(request.getEmail(), user.getRole(), user.getId());

        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(604800);
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(accessToken);
    }

    public ResponseEntity<?> refresh(String refreshToken){
        if(!jwtService.validateToken(refreshToken)){
            return ResponseEntity.badRequest().body("Invalid refresh token!");
        }

        var userId = jwtService.getEmailFromToken(refreshToken);
        var user = userRepository.findByEmail(userId).orElseThrow();
        var accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole(), user.getId());

        return ResponseEntity.ok(accessToken);
    }

}
