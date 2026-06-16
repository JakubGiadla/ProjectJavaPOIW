package com.example.demo.controller;

import com.example.demo.service.EmailService;
import com.example.demo.service.InvitationService;
import com.example.demo.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
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

    // Generuje link na podstawie stałego kodu wydarzenia
    @GetMapping("/link/{eventId}")
    public ResponseEntity<Map<String, String>> getLink(@PathVariable Long eventId, Principal principal) {
        String code = invitationService.getJoinCode(eventId, principal.getName());
        String link = invitationService.getInvitationLink(code);
        return ResponseEntity.ok(Map.of("link", link));
    }

    // Wysyłka kodu e-mailem
    @PostMapping("/send/{eventId}")
    public ResponseEntity<Map<String, String>> sendInvitation(
            @PathVariable Long eventId,
            @RequestParam String email,
            @RequestParam String eventName,
            Principal principal
    ) {
        String code = invitationService.getJoinCode(eventId, principal.getName());
        String link = invitationService.getInvitationLink(code);

        emailService.sendInvitationEmail(email, link, eventName);
        return ResponseEntity.ok(Map.of("message", "Wysłano zaproszenie do: " + email));
    }

    // QR kod oparty na stałym joinCode
    @GetMapping("/qr/{eventId}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable Long eventId, Principal principal) {
        String code = invitationService.getJoinCode(eventId, principal.getName());
        String link = invitationService.getInvitationLink(code);

        byte[] qrBytes = qrCodeService.generateQrCode(link, principal.getName());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrBytes);
    }
}