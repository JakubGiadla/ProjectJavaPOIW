package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@Builder // <--- To wygeneruje metodę builder()
@NoArgsConstructor // <--- Wymagane przez JPA
@AllArgsConstructor // <--- Wymagane przez @Builder
@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private String description;

    @ManyToOne
    private User payer;

    private double amount;

    @ManyToMany
    private List<User> participants;


    // Ręczny konstruktor uwzględniający pole eventId
    public Expense(Long eventId, String description, User payer, double amount, List<User> participants) {
        this.eventId = eventId;
        this.description = description;
        this.payer = payer;
        this.amount = amount;
        this.participants = participants;
    }
}