package org.example.controller;

import org.example.model.Invitation;
import org.example.model.dto.InvitationResponse;
import org.example.service.EmailService;
import org.example.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.service.QrCodeService;

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

    /**
     * POST /api/invitations/generate/5
     * Generuje link zaproszenia dla wydarzenia o ID=5
     */
    @PostMapping("/generate/{eventId}")
    public ResponseEntity<InvitationResponse> generateInvitation(@PathVariable Long eventId) {
        Invitation invitation = invitationService.generateInvitation(eventId);
        String link = invitationService.getInvitationLink(invitation);

        InvitationResponse response = new InvitationResponse(link, invitation.getExpiresAt());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/invitations/send/5?email=jan@example.com&eventName=Urodziny
     * Wysyła zaproszenie mailem (asynchronicznie)
     */
    @PostMapping("/send/{eventId}")
    public ResponseEntity<String> sendInvitation(
            @PathVariable Long eventId,
            @RequestParam String email,
            @RequestParam String eventName
    ) {
        // Generuj link
        Invitation invitation = invitationService.generateInvitation(eventId);
        String link = invitationService.getInvitationLink(invitation);

        // Wyślij mail (działa w tle!)
        emailService.sendInvitationEmail(email, link, eventName);

        return ResponseEntity.ok("Wysyłam zaproszenie do: " + email);
    }

    /**
     * GET /api/invitations/validate/abc-123-token
     * Sprawdza, czy token jest prawidłowy
     */
    @GetMapping("/validate/{token}")
    public ResponseEntity<String> validateInvitation(@PathVariable String token) {
        try {
            Invitation invitation = invitationService.validateToken(token);
            return ResponseEntity.ok("Token prawidłowy dla wydarzenia ID: " + invitation.getEventId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/qr/{token}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String token) {
        // Tutaj tworzymy pełny link, który ma być w kodzie QR
        String link = "https://twoja-apka.pl/join/" + token;

        byte[] qrBytes = qrCodeService.generateQrCode(link);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG) // Mówimy przeglądarce: "To jest obrazek PNG"
                .body(qrBytes);
    }
}