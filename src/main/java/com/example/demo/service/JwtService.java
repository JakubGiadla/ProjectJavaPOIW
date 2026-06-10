package com.example.demo.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // Generujemy bezpieczny klucz kryptograficzny dla algorytmu HS256
    private static final SecretKey KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 86400000; // 24h

    // 1. GENEROWANIE TOKENU (Poprawiono kolejność argumentów w signWith)
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256) // Klucz jako pierwszy argument!
                .compact();
    }

    // 2. PARSOWANIE TOKENU (Metoda potrzebna dla filtra bezpieczeństwa)
    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY) // Weryfikujemy sygnaturę tym samym kluczem
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject(); // Zwraca e-mail zapisany w tokenie jako 'subject'
        } catch (Exception e) {
            // Jeśli token jest uszkodzony, sfałszowany lub wygasł - zwracamy null
            return null;
        }
    }
}