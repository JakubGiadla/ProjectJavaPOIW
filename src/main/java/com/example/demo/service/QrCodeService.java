package com.example.demo.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {

    public byte[] generateQrCode(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Tworzymy macierz kodu QR (250x250 pikseli)
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);

            return baos.toByteArray(); // Zwracamy obrazek jako bajty
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania kodu QR", e);
        }
    }
}