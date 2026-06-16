package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvitationService {

    private static final String BASE_URL = "https://eventsplit.app/join/";

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    // Zwraca po prostu kod dołączania z wydarzenia
    public String getJoinCode(Long eventId, String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Wydarzenie nie istnieje"));

        if (!event.getOrganizer().equals(user)) {
            throw new RuntimeException("Brak uprawnień do uzyskania kodu zaproszenia");
        }

        return event.getJoinCode();
    }

    public String getInvitationLink(String joinCode) {
        return BASE_URL + joinCode;
    }
}