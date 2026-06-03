package com.example.demo.controller;

import com.example.demo.model.Event;
import com.example.demo.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map; // Dodano import

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // GET api/events - lista wszystkich wydarzeń użytkownika
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents(Principal principal) {
        return ResponseEntity.ok(eventService.findAllByUser(principal.getName()));
    }

    // POST api/events - tworzy wydarzenie
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event, Principal principal) {
        return ResponseEntity.ok(eventService.createEvent(event, principal.getName()));
    }

    // GET api/events/{id} - statystyki/szczegóły
    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEventDetails(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.findById(eventId));
    }

    // DELETE api/events/{id}
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    // POST api/events/join - dołączanie przez kod
    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> joinEvent(@RequestParam String joinCode, Principal principal) {
        eventService.joinEvent(joinCode, principal.getName());
        // Zwraca JSON: {"message": "Dołączono do wydarzenia!"}
        return ResponseEntity.ok(Map.of("message", "Dołączono do wydarzenia!"));
    }
}