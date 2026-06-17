package com.example.demo.service;

import com.example.demo.dto.ExpenseRequest;
import com.example.demo.model.Expense;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.repository.ExpenseRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SettlementEngine engine;
    private final EventService eventService;
    private final UserRepository userRepository;

    private final Map<Long, Long> balances = new HashMap<>();

    public ExpenseService(ExpenseRepository expenseRepository, SettlementEngine engine,
                          EventService eventService, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.engine = engine;
        this.eventService = eventService;
        this.userRepository = userRepository;
    }

    public void registerUser(User user) {
        if (user != null && user.getId() != null) {
            balances.putIfAbsent(user.getId(), 0L);
        }
    }

    @Transactional
    public Expense createExpense(Long eventId, ExpenseRequest request, String email) {
        eventService.validateEventIsActive(eventId);
        com.example.demo.model.Event event = eventService.findById(eventId);

        User payer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        List<User> participants = userRepository.findAllById(request.getParticipantIds());

        // 🔥 POPRAWKA: Przekazujemy splitType oraz weights z requestu dalej
        return addExpense(event, request.getTitle(), payer, request.getAmount(), participants, request.getSplitType(), request.getWeights());
    }

    @Transactional
    public Expense addExpense(com.example.demo.model.Event event, String description, User payer, double amount,
                              List<User> participants, String splitType, Map<Long, Double> weights) {
        if (amount <= 0) throw new IllegalArgumentException("Kwota musi być dodatnia");
        if (participants == null || participants.isEmpty())
            throw new IllegalArgumentException("Musi być co najmniej jeden uczestnik");

        Expense newExpense = Expense.builder()
                .event(event)
                .description(description)
                .payer(payer)
                .amount(amount)
                .participants(participants)
                .build();

        expenseRepository.save(newExpense);

        // 🔥 POPRAWKA: Przekazujemy parametry podziału prosto do przetwarzania bilansów
        processExpense(payer.getId(), (long) (amount * 100), participants, splitType, weights);

        return newExpense;
    }

    private void processExpense(Long payerId, long totalAmount, List<User> participants, String splitType, Map<Long, Double> weights) {
        // Płatnik odzyskuje całą wyłożoną kwotę (w groszach)
        balances.put(payerId, balances.getOrDefault(payerId, 0L) + totalAmount);

        String mode = splitType != null ? splitType.toUpperCase() : "EQUAL";
        Map<Long, Double> actualWeights = weights != null ? weights : new HashMap<>();

        if ("PERCENT".equals(mode)) {
            // PODZIAŁ PROCENTOWY
            long distributed = 0;
            for (int i = 0; i < participants.size(); i++) {
                User u = participants.get(i);
                double pct = actualWeights.getOrDefault(u.getId(), 0.0);

                long share = Math.round((totalAmount * pct) / 100.0);

                // Zabezpieczenie zaokrągleń: ostatni uczestnik bierze resztę
                if (i == participants.size() - 1) {
                    share = totalAmount - distributed;
                }
                distributed += share;

                balances.put(u.getId(), balances.getOrDefault(u.getId(), 0L) - share);
            }
        } else if ("AMOUNT".equals(mode)) {
            // PODZIAŁ CO DO GROSZA (Stała kwota wpisana na frontendzie jako zł)
            for (User u : participants) {
                double val = actualWeights.getOrDefault(u.getId(), 0.0);
                long share = (long) (val * 100); // Zamiana na grosze
                balances.put(u.getId(), balances.getOrDefault(u.getId(), 0L) - share);
            }
        } else {
            // TRYB DOMYŚLNY: EQUAL (Równomierny)
            long perPerson = totalAmount / participants.size();
            long remainder = totalAmount % participants.size();

            for (int i = 0; i < participants.size(); i++) {
                User u = participants.get(i);
                long share = perPerson + (i == 0 ? remainder : 0);
                balances.put(u.getId(), balances.getOrDefault(u.getId(), 0L) - share);
            }
        }
    }

    public List<Transaction> calculateSettlements() {
        Map<User, Long> settlementInput = new HashMap<>();
        for (Map.Entry<Long, Long> entry : balances.entrySet()) {
            User user = userRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Użytkownik z bilansu nie istnieje"));
            settlementInput.put(user, entry.getValue());
        }
        return engine.calculate(settlementInput);
    }

    public List<Expense> getHistory(Long eventId) {
        return expenseRepository.findByEventId(eventId);
    }
}