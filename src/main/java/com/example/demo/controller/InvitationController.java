package com.example.demo.controller;

import com.example.demo.dto.InvitationResponse;
import com.example.demo.model.Invitation;
import com.example.demo.service.EmailService;
import com.example.demo.service.InvitationService;
import com.example.demo.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = "*")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private QrCodeService qrCodeService;

    @PostMapping("/generate/{eventId}")
    public ResponseEntity<InvitationResponse> generateInvitation(@PathVariable Long eventId) {
        Invitation invitation = invitationService.generateInvitation(eventId);
        String link = invitationService.getInvitationLink(invitation);

        InvitationResponse response = new InvitationResponse(link, invitation.getExpiresAt());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send/{eventId}")
    public ResponseEntity<Map<String, String>> sendInvitation(
            @PathVariable Long eventId,
            @RequestParam String email,
            @RequestParam String eventName
    ) {
        Invitation invitation = invitationService.generateInvitation(eventId);
        String link = invitationService.getInvitationLink(invitation);

        emailService.sendInvitationEmail(email, link, eventName);

        return ResponseEntity.ok(Map.of("message", "Wysłano zaproszenie do: " + email));
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<Map<String, String>> validateInvitation(@PathVariable String token) {
        try {
            Invitation invitation = invitationService.validateToken(token);
            return ResponseEntity.ok(Map.of(
                    "status", "valid",
                    "eventId", String.valueOf(invitation.getEventId())
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/qr/{token}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String token) {
        String link = "https://apka.pl/join/" + token;
        byte[] qrBytes = qrCodeService.generateQrCode(link);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrBytes);
    }
}