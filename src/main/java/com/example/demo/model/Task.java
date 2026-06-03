package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId; // Żeby wiedzieć, do którego wydarzenia należy
    private String description; // Np. "kup dildo"
    private boolean isCompleted; // Czy zadanie jest zrobione

    @ManyToOne
    private User assignee; // Osoba, której przypisaliśmy zadanie
}