package com.example.TerraFund.services;

import com.example.TerraFund.dto.ChooseRoleRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Service
public class AuthService {
    private UserRepository userRepository;
    private JwtService jwtService;
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

            userRepository.save(user);

            String accessToken = jwtService.generateAccessToken(registerRequest.getEmail(), registerRequest.getRole(), user.getId());
            String refreshToken = jwtService.generateRefreshToken(registerRequest.getEmail(), registerRequest.getRole(), user.getId());

            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
            cookie.setSecure(false); // Set to true in production with HTTPS

            response.addCookie(cookie);
            response.addHeader("Set-Cookie",
                    String.format("refreshToken=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=Lax",
                            refreshToken, 7 * 24 * 60 * 60));

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

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
        cookie.setSecure(false); // Set to true in production with HTTPS

        response.addCookie(cookie);
        response.addHeader("Set-Cookie",
                String.format("refreshToken=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=Lax",
                        refreshToken, 7 * 24 * 60 * 60));

        return ResponseEntity.ok(accessToken);
    }

    public ResponseEntity<?> refresh(String refreshToken){
        if(refreshToken == null || refreshToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found!");
        }

        try {
            if(!jwtService.validateToken(refreshToken)){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token!");
            }

            String email = jwtService.getEmailFromToken(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole(), user.getId());

            return ResponseEntity.ok(accessToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
    }

    public ResponseEntity<?> logout(HttpServletResponse response){
        var cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(false);
        response.addCookie(cookie);

        return ResponseEntity.ok("Logout successful!");
    }

    public ResponseEntity<?> me(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<?> chooseRole(ChooseRoleRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if(user.getRole().equals("LAND_OWNER") || request.getRole().equals("INVESTOR")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change your role;");
        }

        user.setRole(request.getRole());
        userRepository.save(user);

        String newToken = jwtService.generateAccessToken(email, user.getRole(), user.getId());

        return ResponseEntity.ok(newToken);
    }


}
