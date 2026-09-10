package com.example.TerraFund.services;

import com.example.TerraFund.Utils.EmailService;
import com.example.TerraFund.config.CookieProperties;
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
import com.example.TerraFund.security.TokenRevocationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@AllArgsConstructor
@Service
public class AuthService {
    private final InvestorProfileRepository investorProfileRepository;
    private final LandOwnerProfileRepository landOwnerProfileRepository;
    private UserRepository userRepository;
    private JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private CurrentUser currentUser;
    private EmailService emailService;
    private final TokenRevocationService tokenRevocationService;
    private final CookieProperties cookieProperties;

    private ResponseCookie buildRefreshCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }

    private void attachRefreshCookie(HttpServletResponse response, String refreshToken, long maxAgeSeconds) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken, maxAgeSeconds).toString());
    }

    private void recordTokens(User user, String accessToken, String refreshToken) {
        tokenRevocationService.recordIssued(user.getEmail(), jwtService.getJti(accessToken));
        tokenRevocationService.recordIssued(user.getEmail(), jwtService.getJti(refreshToken));
    }

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

            String otp = jwtService.generateOtp();

            user.setEmail(registerRequest.getEmail());
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setOtp(otp);

            userRepository.save(user);

            String accessToken = jwtService.generateAccessToken(registerRequest.getEmail(), user.getRole(), user.getId());
            String refreshToken = jwtService.generateRefreshToken(registerRequest.getEmail(), user.getRole(), user.getId());
            recordTokens(user, accessToken, refreshToken);

            attachRefreshCookie(response, refreshToken, 7 * 24 * 60 * 60);

            //emailService.sendEmail(user.getEmail(), "Verify your account", "Your OTP is: " + otp);

            return ResponseEntity.ok(new RegisterResponse(accessToken));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> verify(VerifyRequest verifyRequest, HttpServletResponse response){
        User user = currentUser.get();

        if(!Objects.equals(user.getOtp(), verifyRequest.getOtp())){
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
        recordTokens(user, accessToken, refreshToken);

        attachRefreshCookie(response, refreshToken, 7 * 24 * 60 * 60);

        return ResponseEntity.ok(accessToken);
    }

    public ResponseEntity<?> refresh(String refreshToken, HttpServletResponse response){
        if(refreshToken == null || refreshToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found!");
        }

        try {
            if(!jwtService.validateToken(refreshToken) || !JwtService.TYPE_REFRESH.equals(jwtService.getTokenType(refreshToken))){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token!");
            }

            if(tokenRevocationService.isRevoked(jwtService.getJti(refreshToken))){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked!");
            }

            String email = jwtService.getEmailFromToken(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Rotate: the presented refresh token can be used only once.
            tokenRevocationService.revoke(jwtService.getJti(refreshToken));

            String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole(), user.getId());
            String newRefreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getRole(), user.getId());
            recordTokens(user, accessToken, newRefreshToken);

            attachRefreshCookie(response, newRefreshToken, 7 * 24 * 60 * 60);

            return ResponseEntity.ok(accessToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
    }

    public ResponseEntity<?> logout(HttpServletResponse response){
        User user = currentUser.get();
        tokenRevocationService.revokeAllForUser(user.getEmail());
        attachRefreshCookie(response, "", 0);
        return ResponseEntity.ok("Logout successful!");
    }

    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email does not exist!"));
        String resetToken = jwtService.generateResetToken(request.getEmail());

        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        //Frontend link to reset password
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;

        emailService.sendEmail(
                user.getEmail(),
                "Password Reset",
                "Click this link to reset your password: " + resetLink
        );

        return ResponseEntity.ok("Password reset link sent to email if it exists.");
    }

    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
        return ResponseEntity.ok("Password has been reset successfully.");
    }

    public ResponseEntity<?> me(){
        User user = currentUser.get();

        if(user.getRole() == RoleEnum.INVESTOR){
            InvestorProfile profile = investorProfileRepository.findByUserEmail(user.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investor profile not found"));

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
        }else if(user.getRole() == RoleEnum.LAND_OWNER){
            LandOwnerProfile profile = landOwnerProfileRepository.findByUserEmail(user.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Land owner profile not found"));

            return ResponseEntity.ok(
                    new LandOwnerProfileResponse(
                            profile.getId(),
                            profile.getFirstName(),
                            profile.getLastName(),
                            profile.getAddress(),
                            profile.getPhoneNumber(),
                            profile.getProfilePictureUrl(),
                            profile.getNationalIdNumber()
                    )
            );
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be an investor or a land owner to access this endpoint!");
        }
    }

    public ResponseEntity<?> chooseRole(ChooseRoleRequest request){
        User user = currentUser.get();

        if(user.getRole() == RoleEnum.INVESTOR || user.getRole() == RoleEnum.LAND_OWNER){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change your role;");
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

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setAddress(request.getAddress());
        profile.setEmail(user.getEmail());
        profile.setPhoneNumber(user.getPhoneNumber());
        profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profile.setNationalIdNumber(request.getNationalIdNumber());
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

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setEmail(user.getEmail());
        profile.setPhoneNumber(user.getPhoneNumber());
        profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profile.setNationalIdNumber(request.getNationalIdNumber());

        profile.setUser(user);

        landOwnerProfileRepository.save(profile);
        return ResponseEntity.ok(
                new LandOwnerProfileResponse(
                        profile.getId(),
                        profile.getFirstName(),
                        profile.getLastName(),
                        profile.getAddress(),
                        profile.getPhoneNumber(),
                        profile.getProfilePictureUrl(),
                        profile.getNationalIdNumber()
                )
        );
    }

    public ResponseEntity<?> updateInvestorProfile(InvestorProfileRequest request){
        User user = currentUser.get();
        InvestorProfile profile = new InvestorProfile();

        if(user.getRole() != RoleEnum.INVESTOR){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be an investor to update a profile!");
        }

        if(!Objects.equals(user.getId(), profile.getUser().getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot update another investor's profile!");
        }

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setAddress(request.getAddress());
        profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profile.setNationalIdNumber(request.getNationalIdNumber());
        profile.setCompany(request.getCompany());
        profile.setOccupation(request.getOccupation());
        profile.setMinInvestmentBudget(request.getMinInvestmentBudget());
        profile.setMaxInvestmentBudget(request.getMaxInvestmentBudget());

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

    public ResponseEntity<?> updateLandOwnerProfile(LandOwnerProfileRequest request){
        User user = currentUser.get();
        LandOwnerProfile profile = new LandOwnerProfile();

        if(user.getRole() != RoleEnum.LAND_OWNER){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be a land owner to update a profile!");
        }

        if(!Objects.equals(user.getId(), profile.getUser().getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot update another land owner's profile!");
        }

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setAddress(request.getAddress());
        profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profile.setNationalIdNumber(request.getNationalIdNumber());
        profile.setPhoneNumber(user.getPhoneNumber());

        return ResponseEntity.ok(
                new LandOwnerProfileResponse(
                        profile.getId(),
                        profile.getFirstName(),
                        profile.getAddress(),
                        profile.getLastName(),
                        profile.getPhoneNumber(),
                        profile.getProfilePictureUrl(),
                        profile.getNationalIdNumber()
                )
        );
    }
}