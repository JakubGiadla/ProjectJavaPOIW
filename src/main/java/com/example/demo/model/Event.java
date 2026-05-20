package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data; // Ta linijka automatycznie wygeneruje getId() w tle!

@Data
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDateTime date;
    private String location;
    private String type;

    @Column(name = "max_attendees")
    private Integer maxAttendees;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;
}