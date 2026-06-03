package com.example.demo.service;

import com.example.demo.model.Expense;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SettlementEngine engine;
    private final Map<User, Long> balances = new HashMap<>();

    public ExpenseService(ExpenseRepository expenseRepository, SettlementEngine engine) {
        this.expenseRepository = expenseRepository;
        this.engine = engine;
    }

    public void registerUser(User user) {
        balances.putIfAbsent(user, 0L);
    }

    @Transactional
    public void addExpense(Long eventId, String description, User payer, double amount, List<User> participants) {
        // 1. Walidacja
        if (amount <= 0) throw new IllegalArgumentException("Kwota musi być dodatnia");
        if (participants == null || participants.isEmpty())
            throw new IllegalArgumentException("Musi być co najmniej jeden uczestnik");

        // 2. Tworzenie obiektu za pomocą Buildera (rozwiązuje błąd konstruktora)
        Expense newExpense = Expense.builder()
                .eventId(eventId)
                .description(description)
                .payer(payer)
                .amount(amount)
                .participants(participants)
                .build();

        // 3. Zapis do bazy
        expenseRepository.save(newExpense);

        // 4. Aktualizacja bilansów
        processExpense(payer, (long) (amount * 100), participants);
    }

    private void processExpense(User payer, long totalAmount, List<User> participants) {
        long perPerson = totalAmount / participants.size();
        long remainder = totalAmount % participants.size();

        balances.put(payer, balances.get(payer) + totalAmount);

        for (int i = 0; i < participants.size(); i++) {
            User u = participants.get(i);
            long share = perPerson + (i == 0 ? remainder : 0);
            balances.put(u, balances.get(u) - share);
        }
    }

    public Map<User, Long> getCurrentBalances() {
        return Collections.unmodifiableMap(balances);
    }

    public List<Transaction> calculateSettlements() {
        return engine.calculate(new HashMap<>(balances));
    }

    public List<Expense> getHistory() {
        return expenseRepository.findAll();
    }
}