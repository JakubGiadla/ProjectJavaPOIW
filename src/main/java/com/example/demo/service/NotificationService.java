package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;

    // Metoda, która może jednocześnie wysłać maila i np. zapisać powiadomienie w DB
    public void notifyUserAboutDebt(String email, String debtorName, double amount) {
        // 1. Wysyłka maila (korzystasz z gotowego EmailService)
        emailService.sendDebtNotification(email, debtorName, amount);

        // 2. Tutaj w przyszłości dodam: notificationRepository.save(...)
        // Aby użytkownik widział powiadomienie wewnątrz aplikacji
        System.out.println("Zarejestrowano powiadomienie dla: " + email);
    }
}