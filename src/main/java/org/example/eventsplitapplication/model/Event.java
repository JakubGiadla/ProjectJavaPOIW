package org.example.eventsplitapplication.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    @ManyToOne // Many events to only one organizer
    @JoinColumn(name = "organizer_id")
    private User organizer;


}
