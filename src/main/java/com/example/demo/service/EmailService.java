package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async  // Ta metoda działa w osobnym wątku!
    public void sendInvitationEmail(String recipientEmail, String invitationLink, String eventName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("Zaproszenie do: " + eventName);
            message.setText(
                    "Cześć!\n\n" +
                            "Zostałeś zaproszony do wydarzenia: " + eventName + "\n" +
                            "Kliknij poniższy link, aby dołączyć:\n\n" +
                            invitationLink + "\n\n" +
                            "Pozdrawiamy,\n" +
                            "Zespół EventSplit"
            );

            mailSender.send(message);
            System.out.println("✅ Mail wysłany do: " + recipientEmail);

        } catch (Exception e) {
            System.err.println("❌ Błąd wysyłki maila: " + e.getMessage());
            // Możesz zapisać błąd do logów lub bazy
        }
    }

    @Async
    public void sendDebtNotification(String recipientEmail, String debtorName, double amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("EventSplit - Nowy dług");
            message.setText(
                    "Cześć!\n\n" +
                            debtorName + " dodał nowy wydatek.\n" +
                            "Twój dług: " + amount + " PLN\n\n" +
                            "Sprawdź szczegóły w aplikacji."
            );

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("❌ Błąd wysyłki powiadomienia: " + e.getMessage());
        }
    }
}