package org.example.controller; // Pamiętaj o spójnej nazwie paczki!

import org.example.service.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private PdfReportService pdfReportService;

    /**
     * GET /api/reports/{eventId}
     * Zwraca plik PDF do przeglądarki
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long eventId) {
        try {
            // Dane, które docelowo pobiore z bazy danych (od Osoby #2)
            String eventName = "Raport Wydarzenia " + eventId;
            double totalCost = 0.0; // Tu trafi suma wydatków

            // Wywołanie Twojego serwisu generującego PDF
            byte[] pdfBytes = pdfReportService.generateEventReport(eventId, eventName, totalCost);

            // Ustawienie nagłówków tak, aby przeglądarka wiedziała, że to plik PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "raport-wydarzenia-" + eventId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}