package com.example.demo.service;

import com.example.demo.dto.AttendeeDTO;
import com.example.demo.dto.EventDetailsDTO;
import com.example.demo.dto.EventReportDTO;
import com.example.demo.dto.EventSummaryDTO; // Dodany import dla DTO podsumowania
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final AttendeeRepository attendeeRepository;
    private final SettlementEngine engine;

    public EventService(EventRepository eventRepository, UserRepository userRepository,
                        ExpenseRepository expenseRepository, AttendeeRepository attendeeRepository,
                        SettlementEngine engine) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.attendeeRepository = attendeeRepository;
        this.engine = engine;
    }

    public void closeEvent(Long eventId, String email) {
        Event event = findById(eventId);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        if (!event.getOrganizer().equals(user)) {
            throw new RuntimeException("Tylko organizator może zamknąć wydarzenie.");
        }
        event.setClosed(true);
        eventRepository.save(event);
    }

    public void deleteEvent(Long eventId, String email) {
        Event event = findById(eventId);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        if (!event.getOrganizer().equals(user)) {
            throw new RuntimeException("Tylko organizator może usunąć wydarzenie.");
        }
        eventRepository.deleteById(eventId);
    }

    public void joinEvent(String joinCode, String email) {
        Event event = eventRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new RuntimeException("Błędny kod wydarzenia"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        if (!attendeeRepository.existsByEventAndUser(event, user)) {
            Attendee attendee = new Attendee(event, user);

            if (event.getAttendees() == null) {
                event.setAttendees(new java.util.ArrayList<>());
            }
            event.getAttendees().add(attendee);

            attendeeRepository.save(attendee);
        }
    }

    public EventDetailsDTO getEventDetails(Long eventId, String email) {
        Event event = getEventWithPermission(eventId, email);

        List<AttendeeDTO> attendeeDTOs = event.getAttendees().stream()
                .map(attendee -> AttendeeDTO.builder()
                        .id(attendee.getId())
                        .userId(attendee.getUser().getId())
                        .name(attendee.getUser().getName())
                        .email(attendee.getUser().getEmail())
                        .joinedAt(attendee.getJoinedAt())
                        .build())
                .toList();

        return EventDetailsDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .date(event.getDate())
                .location(event.getLocation())
                .type(event.getType())
                .joinCode(event.getJoinCode())
                .isClosed(event.isClosed())
                .organizerName(event.getOrganizer() != null ? event.getOrganizer().getName() : null)
                .attendees(attendeeDTOs)
                .build();
    }

    public void validateEventIsActive(Long eventId) {
        Event event = findById(eventId);
        if (event.isClosed()) {
            throw new RuntimeException("Wydarzenie jest zamknięte. Nie można dodawać nowych wydatków.");
        }
    }

    // POPRAWIONA METODA: Zwraca EventSummaryDTO oraz wywołuje nową metodę z repozytorium
    public List<EventSummaryDTO> findAllByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        // Pobiera wydarzenia zorganizowane przez użytkownika LUB te, do których dołączył
        List<Event> events = eventRepository.findAllByOrganizerOrAttendee(user);

        return events.stream()
                .map(event -> EventSummaryDTO.builder()
                        .id(event.getId())
                        .title(event.getTitle())
                        .date(event.getDate())
                        .location(event.getLocation())
                        .type(event.getType())
                        .organizerName(event.getOrganizer() != null ? event.getOrganizer().getName() : null)
                        .build())
                .toList();
    }

    public Event createEvent(Event event, String email) {
        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        event.setOrganizer(organizer);
        String uniqueCode = UUID.randomUUID().toString().replace("-", "");
        event.setJoinCode(uniqueCode);

        return eventRepository.save(event);
    }

    public Event findById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Wydarzenie nie istnieje"));
    }

    public Event getEventWithPermission(Long eventId, String email) {
        Event event = findById(eventId);
        User user = userRepository.findByEmail(email).orElseThrow();

        boolean isOrganizer = event.getOrganizer().equals(user);
        boolean isParticipant = attendeeRepository.existsByEventAndUser(event, user);

        if (!isOrganizer && !isParticipant) {
            throw new RuntimeException("Brak dostępu do tego wydarzenia");
        }
        return event;
    }

    public EventReportDTO generateReport(Long eventId, String email) {
        Event event = getEventWithPermission(eventId, email);

        List<Expense> expenses = event.getExpenses();
        List<Attendee> attendees = event.getAttendees();

        double totalCost = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        Map<Long, User> userCache = new HashMap<>();
        Map<Long, Long> balancesMapLong = new HashMap<>();

        User organizer = event.getOrganizer();
        userCache.put(organizer.getId(), organizer);
        balancesMapLong.put(organizer.getId(), 0L);

        for (Attendee attendee : attendees) {
            User u = attendee.getUser();
            userCache.put(u.getId(), u);
            balancesMapLong.put(u.getId(), 0L);
        }

        for (Expense expense : expenses) {
            User payer = expense.getPayer();
            if (payer == null) continue;

            userCache.put(payer.getId(), payer);
            long amountInCents = (long) (expense.getAmount() * 100);
            List<User> participants = expense.getParticipants();

            if (participants == null || participants.isEmpty()) continue;

            long perPerson = amountInCents / participants.size();
            long remainder = amountInCents % participants.size();

            balancesMapLong.put(payer.getId(), balancesMapLong.getOrDefault(payer.getId(), 0L) + amountInCents);

            for (int i = 0; i < participants.size(); i++) {
                User u = participants.get(i);
                userCache.put(u.getId(), u);
                long share = perPerson + (i == 0 ? remainder : 0);
                balancesMapLong.put(u.getId(), balancesMapLong.getOrDefault(u.getId(), 0L) - share);
            }
        }

        Map<User, Long> settlementInput = new HashMap<>();
        for (Map.Entry<Long, Long> entry : balancesMapLong.entrySet()) {
            settlementInput.put(userCache.get(entry.getKey()), entry.getValue());
        }

        List<Transaction> settlementTransactions = engine.calculate(settlementInput);

        Map<String, Double> balancesPerUser = new HashMap<>();
        for (Map.Entry<Long, Long> entry : balancesMapLong.entrySet()) {
            User u = userCache.get(entry.getKey());
            String userLabel = u.getName() != null ? u.getName() : u.getEmail();
            balancesPerUser.put(userLabel, entry.getValue() / 100.0);
        }

        return EventReportDTO.builder()
                .eventTitle(event.getTitle())
                .expenses(expenses)
                .totalCost(totalCost)
                .balancesPerUser(balancesPerUser)
                .settlementTransactions(settlementTransactions)
                .build();
    }
}