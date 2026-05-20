package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter // Jedna adnotacja tutaj automatycznie tworzy settery dla WSZYSTKICH pól pod spodem
@Entity
@Table(name = "invitations")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "rsvp_status")
    private String rsvpStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used;

    // Pole numeryczne potrzebne do Twojego InvitationService
    @Column(name = "event_id")
    private Long eventId;

    // Relacja obiektowa od drugiej osoby
    @ManyToOne
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private Event event;

    public Invitation() {
        this.createdAt = LocalDateTime.now();
        this.used = false;
    }
}