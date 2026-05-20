package org.example.model.dto;

public class InvitationRequest {
    private Long eventId;
    private String email;
    private String eventName;

    // Gettery i Settery
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
}