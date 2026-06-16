package com.example.demo.service;

import com.example.demo.dto.EventReportDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {
    private final ExpenseRepository expenseRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AttendeeRepository attendeeRepository;
    private final SettlementEngine engine; // DODANO: deklaracja brakującego komponentu

    // Zaktualizowany konstruktor wstrzykujący wszystkie wymagane zależności
    public EventService(EventRepository eventRepository, UserRepository userRepository,
                        ExpenseRepository expenseRepository, AttendeeRepository attendeeRepository,
                        SettlementEngine engine) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.attendeeRepository = attendeeRepository;
        this.engine = engine;
    }

    // --- METODY Z WERYFIKACJĄ ---

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
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        if (!attendeeRepository.existsByEventAndUser(event, user)) {
            Attendee attendee = new Attendee(event, user);
            attendeeRepository.save(attendee);
        }
    }

    // --- METODY POMOCNICZE ---

    public void validateEventIsActive(Long eventId) {
        Event event = findById(eventId);
        if (event.isClosed()) {
            throw new RuntimeException("Wydarzenie jest zamknięte. Nie można dodawać nowych wydatków.");
        }
    }

    public List<Event> findAllByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
        return eventRepository.findByOrganizer(user);
    }

    public Event createEvent(Event event, String email) {
        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
        event.setOrganizer(organizer);
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
        // 1. Weryfikacja dostępu
        Event event = getEventWithPermission(eventId, email);

        // 2. Pobranie wydatków
        List<Expense> expenses = expenseRepository.findByEventId(eventId);

        // 3. Obliczenie kosztu całkowitego
        double totalCost = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        // Mapy pomocnicze: dopasowanie ID użytkownika do jego obiektu oraz gromadzenie bilansów (w groszach)
        Map<Long, User> userCache = new HashMap<>();
        Map<Long, Long> balancesMapLong = new HashMap<>();

        // Inicjalizacja uczestników: Organizator
        User organizer = event.getOrganizer();
        userCache.put(organizer.getId(), organizer);
        balancesMapLong.put(organizer.getId(), 0L);

        // Inicjalizacja uczestników: Zapisani goście
        List<Attendee> attendees = attendeeRepository.findByEvent(event);
        for (Attendee attendee : attendees) {
            User u = attendee.getUser();
            userCache.put(u.getId(), u);
            balancesMapLong.put(u.getId(), 0L);
        }

        // 4. Przetwarzanie kosztów wydatków na bazie ID użytkowników
        for (Expense expense : expenses) {
            User payer = expense.getPayer();
            if (payer == null) continue;

            // Rejestrujemy płatnika w cache, gdyby nie był wcześniej zainicjalizowany
            userCache.put(payer.getId(), payer);

            long amountInCents = (long) (expense.getAmount() * 100);
            List<User> participants = expense.getParticipants();

            if (participants == null || participants.isEmpty()) {
                continue;
            }

            long perPerson = amountInCents / participants.size();
            long remainder = amountInCents % participants.size();

            // Płatnik otrzymuje zwrot pełnej kwoty
            balancesMapLong.put(payer.getId(), balancesMapLong.getOrDefault(payer.getId(), 0L) + amountInCents);

            // Pobranie części kosztów od uczestników
            for (int i = 0; i < participants.size(); i++) {
                User u = participants.get(i);
                userCache.put(u.getId(), u); // Zabezpieczenie cache

                long share = perPerson + (i == 0 ? remainder : 0);
                balancesMapLong.put(u.getId(), balancesMapLong.getOrDefault(u.getId(), 0L) - share);
            }
        }

        // Przygotowanie mapy obiektowej dla SettlementEngine (wymaganej przez sygnaturę metody calculate)
        Map<User, Long> settlementInput = new HashMap<>();
        for (Map.Entry<Long, Long> entry : balancesMapLong.entrySet()) {
            settlementInput.put(userCache.get(entry.getKey()), entry.getValue());
        }

        // 5. Obliczenie transakcji wyrównawczych przez silnik rozliczeniowy
        List<Transaction> settlementTransactions = engine.calculate(settlementInput);

        // 6. Konwersja danych bilansowych do formatu tekstowego dla DTO (PLN)
        Map<String, Double> balancesPerUser = new HashMap<>();
        for (Map.Entry<Long, Long> entry : balancesMapLong.entrySet()) {
            User u = userCache.get(entry.getKey());
            String userLabel = u.getName() != null ? u.getName() : u.getEmail();
            double balanceInPln = entry.getValue() / 100.0;
            balancesPerUser.put(userLabel, balanceInPln);
        }

        // 7. Zwrócenie kompletnego DTO raportu
        return EventReportDTO.builder()
                .eventTitle(event.getTitle())
                .expenses(expenses)
                .totalCost(totalCost)
                .balancesPerUser(balancesPerUser)
                .settlementTransactions(settlementTransactions)
                .build();
    }
}