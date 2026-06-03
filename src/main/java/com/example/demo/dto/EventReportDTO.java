package com.example.demo.dto;

import com.example.demo.model.Expense;
import com.example.demo.model.Transaction;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class EventReportDTO {
    private String eventTitle;
    private List<Expense> expenses;
    private double totalCost;
    private Map<String, Double> balancesPerUser;
    private List<Transaction> settlementTransactions;
}