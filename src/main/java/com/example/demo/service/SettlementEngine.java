package com.example.demo.service; // Ważne: zmieniony pakiet z org.example!

import com.example.demo.model.Transaction; // Import Twojego rekordu Transaction
import com.example.demo.model.User;        // Import Twojego rekordu User
import org.springframework.stereotype.Component; // Import adnotacji Springa

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component // Dzięki temu Spring pozwoli na automatyczne wstrzyknięcie tej klasy w ExpenseService
public class SettlementEngine {

    public List<Transaction> calculate(Map<User, Long> balances) {
        List<Transaction> settlements = new ArrayList<>();

        // Rozdzielamy na dłużników (ujemny balans) i wierzycieli (dodatni)
        List<Map.Entry<User, Long>> debtors = new ArrayList<>();
        List<Map.Entry<User, Long>> creditors = new ArrayList<>();

        for (var entry : balances.entrySet()) {
            if (entry.getValue() < 0) debtors.add(entry);
            else if (entry.getValue() > 0) creditors.add(entry);
        }

        int d = 0, c = 0;
        while (d < debtors.size() && c < creditors.size()) {
            var debtor = debtors.get(d);
            var creditor = creditors.get(c);

            long amountToPay = Math.min(-debtor.getValue(), creditor.getValue());

            settlements.add(new Transaction(
                    debtor.getKey(),
                    creditor.getKey(),
                    amountToPay / 100.0 // Powrót z groszy na złote do wyświetlenia
            ));

            // Aktualizujemy pozostałe kwoty
            debtor.setValue(debtor.getValue() + amountToPay);
            creditor.setValue(creditor.getValue() - amountToPay);

            if (debtor.getValue() == 0) d++;
            if (creditor.getValue() == 0) c++;
        }

        return settlements;
    }
}