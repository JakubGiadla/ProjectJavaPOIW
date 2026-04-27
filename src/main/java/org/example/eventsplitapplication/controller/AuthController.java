package org.example.eventsplitapplication.controller;

import org.example.eventsplitapplication.dto.LoginRequest;
import org.example.eventsplitapplication.model.User;
import org.example.eventsplitapplication.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Główny adres dla logowania i restracji
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * To wywoła Flutter/React, gdy użytkownik wpisze dane w oknie logowania
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // AuthService sprawdzi bazę w Dockerze i wygeneruje JWT
            String token = authService.login(loginRequest);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            // Jeśli hasło złe lub brak usera - wysyłamy błąd 401 (Unauthorized)
            return ResponseEntity.status(401).body("Błąd logowania: " + e.getMessage());
        }
    }

    /**
     * To wywoła frontend, gdy użytkownik wypełni formularz "Zarejestruj się"
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = authService.register(user);
            return ResponseEntity.ok("Użytkownik zarejestrowany pomyślnie: " + registeredUser.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd rejestracji: " + e.getMessage());
        }
    }
}
