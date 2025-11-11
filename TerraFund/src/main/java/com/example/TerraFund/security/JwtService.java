package com.example.TerraFund.security;

import com.example.TerraFund.dto.enums.RoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String secret;

    public String generateAccessToken(String email, RoleEnum role, Long id){
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("id", id)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 300))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact()
                ;
    }

    public String generateRefreshToken(String email, RoleEnum role, Long id){
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("id", id)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 604800))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact()
                ;
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
}