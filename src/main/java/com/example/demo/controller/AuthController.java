package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.ProfileUpdateRequest;
import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") // Główny adres dla operacji autoryzacji
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * To wywoła Flutter, gdy użytkownik wpisze dane w oknie logowania
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // AuthService sprawdzi bazę i wygeneruje token JWT
            String token = authService.login(loginRequest);
            // Zwracamy JWT w JSON: {"token": "..."}
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            // Jeśli hasło złe lub brak usera - wysyłamy błąd 401
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * To wywoła frontend, gdy użytkownik wypełni formularz "Zarejestruj się"
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = authService.register(user);
            return ResponseEntity.ok(Map.of("message", "Użytkownik zarejestrowany: " + registeredUser.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DO ZAPISYWANIA NAZWY PROFILU
     * Flutter przesyła dane w JSON-ie, my aktualizujemy profil
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request, Principal principal) {
        try {
            // principal.getName() wyciąga email z tokenu JWT
            authService.updateProfile(principal.getName(), request);
            return ResponseEntity.ok(Map.of("message", "Profil zaktualizowany."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ZMIANA HASŁA
     * Flutter przesyła stare i nowe hasło, my je weryfikujemy i zmieniamy
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        try {
            authService.changePassword(principal.getName(), request);
            return ResponseEntity.ok(Map.of("message", "Hasło zmienione."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * USUNIĘCIE KONTA
     * Flutter woła to, gdy użytkownik chce skasować swoje konto
     */
    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUser(Principal principal) {
        try {
            authService.deleteUser(principal.getName());
            return ResponseEntity.ok(Map.of("message", "Konto usunięte."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}