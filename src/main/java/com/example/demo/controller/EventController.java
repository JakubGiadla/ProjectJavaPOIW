package com.example.demo.controller;

import com.example.demo.dto.EventDetailsDTO;
import com.example.demo.dto.EventSummaryDTO; // Import nowo utworzonego obiektu DTO
import com.example.demo.model.Event;
import com.example.demo.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Kontroler obsługujący główne operacje na wydarzeniach (Events).
 * Wszystkie endpointy są zabezpieczone i wymagają autoryzacji użytkownika (JWT).
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * GET /api/events
     * Pobiera listę wszystkich wydarzeń powiązanych z zalogowanym użytkownikiem (w których uczestniczy lub jest organizatorem).
     * Wykorzystuje principal.getName() (email) do zidentyfikowania użytkownika.
     */
    @GetMapping
    public ResponseEntity<List<EventSummaryDTO>> getAllEvents(Principal principal) {
        // Zmieniono typ zwracany z List<Event> na List<EventSummaryDTO>, aby dopasować do serwisu
        return ResponseEntity.ok(eventService.findAllByUser(principal.getName()));
    }

    /**
     * POST /api/events
     * Inicjalizuje nowe wydarzenie w systemie.
     * Użytkownik wysyłający żądanie zostaje automatycznie przypisany jako organizator.
     */
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event, Principal principal) {
        return ResponseEntity.ok(eventService.createEvent(event, principal.getName()));
    }

    /**
     * GET /api/events/{eventId}
     * Zwraca szczegółowe informacje o wybranym wydarzeniu wraz z listą uczestników.
     * Serwis weryfikuje, czy użytkownik ma prawo wglądu (czy jest uczestnikiem lub organizatorem).
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailsDTO> getEventDetails(@PathVariable Long eventId, Principal principal) {
        return ResponseEntity.ok(eventService.getEventDetails(eventId, principal.getName()));
    }

    /**
     * DELETE /api/events/{eventId}
     * Usuwa wydarzenie z bazy danych.
     * Operacja dostępna tylko dla właściciela (organizatora) wydarzenia.
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId, Principal principal) {
        eventService.deleteEvent(eventId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/events/join
     * Umożliwia dołączenie do wydarzenia za pomocą unikalnego kodu zaproszenia.
     * Automatycznie tworzy powiązanie między użytkownikiem a wydarzeniem.
     */
    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> joinEvent(@RequestParam String joinCode, Principal principal) {
        eventService.joinEvent(joinCode, principal.getName());
        return ResponseEntity.ok(Map.of("message", "Dołączono do wydarzenia!"));
    }

    /**
     * POST /api/events/{eventId}/close
     * Finalizuje wydarzenie – po zamknięciu nie można dodawać nowych wydatków.
     * Wymaga uprawnień organizatora.
     */
    @PostMapping("/{eventId}/close")
    public ResponseEntity<String> closeEvent(@PathVariable Long eventId, Principal principal) {
        try {
            eventService.closeEvent(eventId, principal.getName());
            return ResponseEntity.ok("Wydarzenie zostało pomyślnie zamknięte.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}