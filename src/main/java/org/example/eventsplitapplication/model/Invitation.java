package org.example.eventsplitapplication.model;



import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "rsvp_status")
    private String rsvpStatus;

    @ManyToOne // Many invitations to only one event
    @JoinColumn(name = "event_id")
    private Event event;


}
