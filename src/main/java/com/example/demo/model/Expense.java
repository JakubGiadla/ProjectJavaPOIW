package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event; // Teraz używasz obiektu, a nie ID!

    private String description;

    @ManyToOne
    private User payer;

    private double amount;

    @ManyToMany
    private List<User> participants;
}