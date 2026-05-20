package com.example.demo.controller;


import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-debt")
    public ResponseEntity<String> notifyDebt(
            @RequestParam String email,
            @RequestParam String debtorName, // Dodałem to, żeby nie było na sztywno "Użytkownik"
            @RequestParam double amount
    ) {
        // Wywołujemy asynchroniczną metodę z EmailService
        emailService.sendDebtNotification(email, debtorName, amount);
        //Mailtrap.io podlaczyc pod wysylanie meili chyba ze bedziemy miec zalozony jakis statyczny dla apki ddykowany mail
        return ResponseEntity.ok("Powiadomienie o długu (" + amount + " PLN) zostało zakolejkowane do wysyłki.");
    }
}