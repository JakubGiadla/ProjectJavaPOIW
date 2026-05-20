package org.example.service;

import org.example.model.Invitation;
import org.example.repository.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InvitationService {

    private static final String BASE_URL = "https://eventsplit.app/join/";

    @Autowired
    private InvitationRepository invitationRepository;

    public Invitation generateInvitation(Long eventId) {
        // Generuj token
        String token = UUID.randomUUID().toString();

        // Stwórz zaproszenie
        Invitation invitation = new Invitation();
        invitation.setEventId(eventId);
        invitation.setToken(token);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // Ważne 7 dni

        // Zapisz w bazie
        return invitationRepository.save(invitation);
    }

    public String getInvitationLink(Invitation invitation) {
        return BASE_URL + invitation.getToken();
    }

    public Invitation validateToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Nieprawidłowy token"));

        // Sprawdź, czy nie wygasł
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link wygasł");
        }

        if (invitation.isUsed()) {
            throw new RuntimeException("Link został już wykorzystany");
        }

        return invitation;
    }
}