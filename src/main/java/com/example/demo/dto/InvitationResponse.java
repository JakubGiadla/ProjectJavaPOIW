package com.example.demo.dto;

import java.time.LocalDateTime;

public class InvitationResponse {
    private String invitationLink;
    private LocalDateTime expiresAt;

    public InvitationResponse(String invitationLink, LocalDateTime expiresAt) {
        this.invitationLink = invitationLink;
        this.expiresAt = expiresAt;
    }

    // Gettery i settery
    public String getInvitationLink() { return invitationLink; }
    public void setInvitationLink(String link) { this.invitationLink = link; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}