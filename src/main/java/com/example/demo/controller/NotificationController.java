package com.example.demo.controller;

import com.example.demo.service.EmailService;
import com.example.demo.service.NotificationService; // Zakładam, że masz ten serwis
import com.example.demo.model.Notification;          // Oraz model
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    /**
     * POST /api/notifications/send-debt
     * Wysyła powiadomienie o długu do użytkownika
     */
    @PostMapping("/send-debt")
    public ResponseEntity<Map<String, String>> notifyDebt(
            @RequestParam String email,
            @RequestParam String debtorName,
            @RequestParam double amount
    ) {
        emailService.sendDebtNotification(email, debtorName, amount);
        return ResponseEntity.ok(Map.of("message", "Powiadomienie o długu (" + amount + " PLN) zostało zakolejkowane do wysyłki."));
    }

    /**
     * GET /api/notifications
     * Pobiera listę wszystkich powiadomień dla zalogowanego użytkownika
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.findAllByUser(principal.getName()));
    }

    /**
     * PUT /api/notifications/mark-read/{id}
     * Oznacza konkretne powiadomienie jako przeczytane
     */
    @PutMapping("/mark-read/{id}")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Powiadomienie oznaczone jako przeczytane"));
    }

    /**
     * PUT /api/notifications/mark-all-read
     * Guziczek "odczytaj wszystkie" w apce
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, String>> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        return ResponseEntity.ok(Map.of("message", "Wszystkie powiadomienia oznaczone jako przeczytane"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(Map.of("message", "Powiadomienie usunięte."));
    }
}