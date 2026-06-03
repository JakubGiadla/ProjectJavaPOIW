package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.User;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // Pobiera wszystkie wydarzenia przypisane do danego e-maila (użytkownika)
    public List<Event> findAllByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
        return eventRepository.findByOrganizer(user);
    }

    // Tworzy nowe wydarzenie
    public Event createEvent(Event event, String email) {
        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }

    // Znajduje wydarzenie po ID
    public Event findById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Wydarzenie nie istnieje"));
    }

    // Usuwa wydarzenie
    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
    }

    // Logika dołączania (musisz mieć pole 'joinCode' w klasie Event!)
    public void joinEvent(String joinCode, String email) {
        Event event = eventRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new RuntimeException("Błędny kod wydarzenia"));
        // Tutaj dodaj logikę dodawania usera do uczestników wydarzenia
    }
}