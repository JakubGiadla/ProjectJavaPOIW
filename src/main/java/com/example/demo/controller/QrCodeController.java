package com.example.demo.controller;

import com.example.demo.service.QrCodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    // Endpoint, który po wywołaniu wygeneruje kod QR dla danego tekstu/kodu
    @GetMapping("/{text}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String text) {
        byte[] qrImage = qrCodeService.generateQrCode(text);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG) // Ważne: informujemy przeglądarkę/appkę, że to PNG
                .body(qrImage);
    }
}