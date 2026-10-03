package com.example.TerraFund.security;

import com.example.TerraFund.dto.enums.RoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    // SECURITY: SecureRandom instead of Math.random() (predictable OTPs)
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    @Value("${application.security.jwt.secret-key}")
    private String secret;

    /**
     * SECURITY: tokens carry a "type" claim so an access token can never be
     * replayed as a refresh token (and vice versa).
     */
    public String generateAccessToken(String email, RoleEnum role, Long id){
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("id", id)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 minutes
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact()
                ;
    }

    public String generateRefreshToken(String email, RoleEnum role, Long id){
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("id", id)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 days
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact()
                ;
    }

    public String getTokenType(String token){
        return getClaims(token).get(CLAIM_TYPE, String.class);
    }

    public boolean isAccessToken(String token){
        return TYPE_ACCESS.equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token){
        return TYPE_REFRESH.equals(getTokenType(token));
    }

    public boolean validateToken(String token){
        try{
            var claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        }catch (JwtException e){
            return false;
        }
    }

    public Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmailFromToken(String token){
        return getClaims(token).getSubject();
    }

    public String getRoleFromToken(String token){
        return getClaims(token).get("role", String.class);
    }

    public String generateOtp(){
        return String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));
    }

    public String generateResetToken(String email) {
        return UUID.randomUUID().toString();
    }
}
