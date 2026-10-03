package com.example.TerraFund.services;

import com.example.TerraFund.Utils.EmailService;
import com.example.TerraFund.dto.enums.RoleEnum;
import com.example.TerraFund.dto.requests.*;
import com.example.TerraFund.dto.responses.InvestorProfileResponse;
import com.example.TerraFund.dto.responses.LandOwnerProfileResponse;
import com.example.TerraFund.dto.responses.RegisterResponse;
import com.example.TerraFund.entities.InvestorProfile;
import com.example.TerraFund.entities.LandOwnerProfile;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.InvestorProfileRepository;
import com.example.TerraFund.repositories.LandOwnerProfileRepository;
import com.example.TerraFund.repositories.UserRepository;
import com.example.TerraFund.security.CurrentUser;
import com.example.TerraFund.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final InvestorProfileRepository investorProfileRepository;
    private final LandOwnerProfileRepository landOwnerProfileRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;
    private final EmailService emailService;

    // SECURITY: set to true in production so the refresh cookie is never sent over plain HTTP.
    @Value("${application.security.cookie-secure:false}")
    private boolean cookieSecure;

    /** Base URL of the frontend, used to build password-reset links. */
    @Value("${application.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    private static final String REFRESH_COOKIE = "refreshToken";
    private static final int REFRESH_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    /**
     * SECURITY: single Set-Cookie header (previously the cookie was set twice,
     * once without SameSite), HttpOnly + SameSite=Lax always, Secure configurable.
     */
    private void setRefreshCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie",
                String.format("%s=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=Lax%s",
                        REFRESH_COOKIE, token, REFRESH_COOKIE_MAX_AGE, cookieSecure ? "; Secure" : ""));
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                String.format("%s=; Path=/; HttpOnly; Max-Age=0; SameSite=Lax%s",
                        REFRESH_COOKIE, cookieSecure ? "; Secure" : ""));
    }

    public ResponseEntity<?> register(RegisterRequest registerRequest, HttpServletResponse response){
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists!");
        }

        if(registerRequest.getPassword().length() < 8){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long!");
        }

        if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match!");
        }

        try{
            User user = new User();

            String otp = jwtService.generateOtp();

            user.setEmail(registerRequest.getEmail());
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setOtp(otp);

            userRepository.save(user);

            String accessToken = jwtService.generateAccessToken(registerRequest.getEmail(), user.getRole(), user.getId());
            String refreshToken = jwtService.generateRefreshToken(registerRequest.getEmail(), user.getRole(), user.getId());

            setRefreshCookie(response, refreshToken);

            // SECURITY: the OTP is delivered by email, never in the HTTP response
            // (previously anyone could "verify" any account without inbox access).
            // A mail outage must not block registration, so failures are logged only.
            try {
                emailService.sendEmail(user.getEmail(), "Verify your account", "Your OTP is: " + otp);
            } catch (Exception mailEx) {
                log.error("Failed to send verification OTP to {}: {}", user.getEmail(), mailEx.getMessage());
            }

            return ResponseEntity.ok(new RegisterResponse(accessToken));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> verify(VerifyRequest verifyRequest, HttpServletResponse response){
        User user = currentUser.get();

        if(user.getOtp() == null || verifyRequest.getOtp() == null
                || !user.getOtp().equals(verifyRequest.getOtp())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP!");
        }
        user.setOtp(null);
        user.setOtpVerified(true);
        userRepository.save(user);
        return ResponseEntity.ok("OTP verified successfully!");
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

        setRefreshCookie(response, refreshToken);

        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("accessToken", accessToken);
        res.put("token", accessToken);
        res.put("tokenType", "Bearer");
        res.put("id", user.getId());
        res.put("email", user.getEmail());
        res.put("role", user.getRole() != null ? user.getRole().name().toLowerCase() : "user");
        java.util.Map<String, Object> userMap = new java.util.HashMap<>();
        userMap.put("id", String.valueOf(user.getId()));
        userMap.put("email", user.getEmail());
        userMap.put("name", user.getEmail().split("@")[0]);
        userMap.put("phone", user.getPhoneNumber());
        userMap.put("role", user.getRole() != null ? user.getRole().name().toLowerCase() : "user");
        userMap.put("kyc_status", Boolean.TRUE.equals(user.getOtpVerified()) ? "verified" : "pending");
        res.put("user", userMap);

        return ResponseEntity.ok(res);
    }

    public ResponseEntity<?> refresh(String refreshToken){
        if(refreshToken == null || refreshToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found!");
        }

        try {
            if(!jwtService.validateToken(refreshToken) || !jwtService.isRefreshToken(refreshToken)){
                // SECURITY: a stolen 15-minute ACCESS token can no longer be
                // replayed to /refresh to mint new sessions.
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
        clearRefreshCookie(response);
        return ResponseEntity.ok("Logout successful!");
    }

    /**
     * SECURITY: the response is identical whether or not the email exists, to
     * prevent user enumeration (previously threw a 500 "Email does not exist!").
     */
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String resetToken = jwtService.generateResetToken(request.getEmail());

            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);

            // Frontend link to reset password (base URL configurable per environment)
            String resetLink = frontendBaseUrl + "/reset-password?token=" + resetToken;

            emailService.sendEmail(
                    user.getEmail(),
                    "Password Reset",
                    "Click this link to reset your password: " + resetLink
            );
        });

        return ResponseEntity.ok("Password reset link sent to email if it exists.");
    }

    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
        return ResponseEntity.ok("Password has been reset successfully.");
    }

    public ResponseEntity<?> me(){
        User user = currentUser.get();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        if(user.getRole() == RoleEnum.INVESTOR){
            var profileOpt = investorProfileRepository.findByUserEmail(user.getEmail());
            if (profileOpt.isPresent()) {
                InvestorProfile profile = profileOpt.get();
                return ResponseEntity.ok(
                        new InvestorProfileResponse(
                                profile.getId(),
                                profile.getFirstName(),
                                profile.getAddress(),
                                profile.getLastName(),
                                profile.getPhoneNumber(),
                                profile.getProfilePictureUrl(),
                                profile.getNationalIdNumber(),
                                profile.getCompany(),
                                profile.getOccupation(),
                                profile.getMinInvestmentBudget(),
                                profile.getMaxInvestmentBudget()
                        )
                );
            }
        }else if(user.getRole() == RoleEnum.LAND_OWNER){
            var profileOpt = landOwnerProfileRepository.findByUserEmail(user.getEmail());
            if (profileOpt.isPresent()) {
                LandOwnerProfile profile = profileOpt.get();
                return ResponseEntity.ok(
                        new LandOwnerProfileResponse(
                                profile.getId(),
                                profile.getFirstName(),
                                profile.getLastName(),
                                profile.getEmail(),
                                profile.getPhoneNumber(),
                                profile.getAddress(),
                                profile.getProfilePictureUrl(),
                                profile.getNationalIdNumber()
                        )
                );
            }
        }

        java.util.Map<String, Object> baseUser = new java.util.HashMap<>();
        baseUser.put("id", user.getId());
        baseUser.put("email", user.getEmail());
        baseUser.put("name", user.getEmail().split("@")[0]);
        baseUser.put("phoneNumber", user.getPhoneNumber());
        baseUser.put("role", user.getRole() != null ? user.getRole().name().toLowerCase() : "user");
        baseUser.put("kyc_status", Boolean.TRUE.equals(user.getOtpVerified()) ? "verified" : "pending");
        baseUser.put("hasProfile", false);
        return ResponseEntity.ok(baseUser);
    }

    /**
     * SECURITY: only INVESTOR and LAND_OWNER roles can be chosen. Previously any
     * client could send {"role": "ADMIN"} here and escalate to full admin.
     */
    public ResponseEntity<?> chooseRole(ChooseRoleRequest request){
        User user = currentUser.get();

        if(user.getRole() == RoleEnum.INVESTOR || user.getRole() == RoleEnum.LAND_OWNER){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change your role;");
        }

        if(request.getRole() != RoleEnum.INVESTOR && request.getRole() != RoleEnum.LAND_OWNER){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only choose the INVESTOR or LAND_OWNER role!");
        }

        user.setRole(request.getRole());
        userRepository.save(user);

        String newToken = jwtService.generateAccessToken(user.getEmail(), user.getRole(), user.getId());

        return ResponseEntity.ok(newToken);
    }

    public ResponseEntity<?> createInvestorProfile(InvestorProfileRequest request){
        User user = currentUser.get();

        if(user.getRole() != RoleEnum.INVESTOR){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be an investor to create a profile!");
        }

        InvestorProfile profile = new InvestorProfile();

        profile.setFirstName(request.getFirstName() != null ? request.getFirstName() : "");
        profile.setLastName(request.getLastName() != null ? request.getLastName() : "");
        profile.setAddress(request.getAddress() != null ? request.getAddress() : "Kigali, Rwanda");
        profile.setEmail(user.getEmail());
        profile.setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profile.setNationalIdNumber(request.getNationalIdNumber() != null ? request.getNationalIdNumber() : "N/A");
        profile.setCompany(request.getCompany());
        profile.setOccupation(request.getOccupation());
        profile.setMinInvestmentBudget(request.getMinInvestmentBudget());
        profile.setMaxInvestmentBudget(request.getMaxInvestmentBudget());
        profile.setUser(user);

        investorProfileRepository.save(profile);
        return ResponseEntity.ok(
                new InvestorProfileResponse(
                        profile.getId(),
                        profile.getFirstName(),
                        profile.getAddress(),
                        profile.getLastName(),
                        profile.getPhoneNumber(),
                        profile.getProfilePictureUrl(),
                        profile.getNationalIdNumber(),
                        profile.getCompany(),
                        profile.getOccupation(),
                        profile.getMinInvestmentBudget(),
                        profile.getMaxInvestmentBudget()
                )
        );
    }

    public ResponseEntity<?> createLandOwnerProfile(LandOwnerProfileRequest request){
        User user = currentUser.get();

        if(user.getRole() != RoleEnum.LAND_OWNER){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be a land owner to create a profile!");
        }

        LandOwnerProfile profile = new LandOwnerProfile();

        profile.setFirstName(request.getFirstName() != null ? request.getFirstName() : "");
        profile.setLastName(request.getLastName() != null ? request.getLastName() : "");
        profile.setEmail(user.getEmail());
        profile.setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        profile.setAddress(request.getAddress() != null ? request.getAddress() : "Kigali, Rwanda");
        profile.setTotalLandsListed("0");
        profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profile.setNationalIdNumber(request.getNationalIdNumber() != null ? request.getNationalIdNumber() : "N/A");

        profile.setUser(user);

        landOwnerProfileRepository.save(profile);
        return ResponseEntity.ok(
                new LandOwnerProfileResponse(
                        profile.getId(),
                        profile.getFirstName(),
                        profile.getLastName(),
                        profile.getEmail(),
                        profile.getPhoneNumber(),
                        profile.getAddress(),
                        profile.getProfilePictureUrl(),
                        profile.getNationalIdNumber()
                )
        );
    }

    /**
     * SECURITY/BUG FIX: the previous implementation created a brand-new empty
     * profile, dereferenced its null user (guaranteed NullPointerException /
     * HTTP 500) and never persisted anything. It now loads the caller's own
     * profile and updates it.
     */
    public ResponseEntity<?> updateInvestorProfile(InvestorProfileRequest request){
        User user = currentUser.get();

        if(user.getRole() != RoleEnum.INVESTOR){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be an investor to update a profile!");
        }

        InvestorProfile profile = investorProfileRepository.findByUserEmail(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investor profile not found"));

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getProfilePictureUrl() != null) profile.setProfilePictureUrl(request.getProfilePictureUrl());
        if (request.getNationalIdNumber() != null) profile.setNationalIdNumber(request.getNationalIdNumber());
        if (request.getCompany() != null) profile.setCompany(request.getCompany());
        if (request.getOccupation() != null) profile.setOccupation(request.getOccupation());
        if (request.getMinInvestmentBudget() != null) profile.setMinInvestmentBudget(request.getMinInvestmentBudget());
        if (request.getMaxInvestmentBudget() != null) profile.setMaxInvestmentBudget(request.getMaxInvestmentBudget());

        investorProfileRepository.save(profile);

        return ResponseEntity.ok(
                new InvestorProfileResponse(
                        profile.getId(),
                        profile.getFirstName(),
                        profile.getAddress(),
                        profile.getLastName(),
                        profile.getPhoneNumber(),
                        profile.getProfilePictureUrl(),
                        profile.getNationalIdNumber(),
                        profile.getCompany(),
                        profile.getOccupation(),
                        profile.getMinInvestmentBudget(),
                        profile.getMaxInvestmentBudget()
                )
        );
    }

    /** Same fix as updateInvestorProfile: load the caller's profile and persist. */
    public ResponseEntity<?> updateLandOwnerProfile(LandOwnerProfileRequest request){
        User user = currentUser.get();

        if(user.getRole() != RoleEnum.LAND_OWNER){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be a land owner to update a profile!");
        }

        LandOwnerProfile profile = landOwnerProfileRepository.findByUserEmail(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Land owner profile not found"));

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getProfilePictureUrl() != null) profile.setProfilePictureUrl(request.getProfilePictureUrl());
        if (request.getNationalIdNumber() != null) profile.setNationalIdNumber(request.getNationalIdNumber());

        landOwnerProfileRepository.save(profile);

        return ResponseEntity.ok(
                new LandOwnerProfileResponse(
                        profile.getId(),
                        profile.getFirstName(),
                        profile.getLastName(),
                        profile.getEmail(),
                        profile.getPhoneNumber(),
                        profile.getAddress(),
                        profile.getProfilePictureUrl(),
                        profile.getNationalIdNumber()
                )
        );
    }
}
