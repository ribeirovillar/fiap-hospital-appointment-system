package com.fiap.hospital.auth.infrastructure.adapters.security;

import com.fiap.hospital.auth.infrastructure.config.JwtConfiguration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtTokenAdapter {

    @Autowired
    private JwtConfiguration jwtConfiguration;
    
    private SecretKey signingKey;
    
    private synchronized SecretKey getSigningKey() {
        if (signingKey == null) {
            String secret = jwtConfiguration.getSecret();
            byte[] keyBytes;
            if (secret.length() < 32) {
                StringBuilder paddedSecret = new StringBuilder(secret);
                while (paddedSecret.length() < 32) {
                    paddedSecret.append("0");
                }
                keyBytes = paddedSecret.toString().getBytes(StandardCharsets.UTF_8);
            } else {
                keyBytes = secret.substring(0, 32).getBytes(StandardCharsets.UTF_8);
            }
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return signingKey;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfiguration.getExpiration() * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
} 