package com.example.demo.controller;

import com.example.demo.model.Expense;
import com.example.demo.repository.ExpenseRepository;
import com.example.demo.service.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final PdfReportService pdfReportService;
    private final ExpenseRepository expenseRepository;

    // Używamy wstrzykiwania przez konstruktor (zalecane w Springu zamiast @Autowired na polu)
    public ReportController(PdfReportService pdfReportService, ExpenseRepository expenseRepository) {
        this.pdfReportService = pdfReportService;
        this.expenseRepository = expenseRepository;
    }

    /**
     * GET /api/reports/{eventId}
     * Zwraca plik PDF z danymi pobranymi z bazy danych
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long eventId) {
        try {
            // 1. Pobieramy wydatki z bazy dla danego wydarzenia
            List<Expense> expenses = expenseRepository.findByEventId(eventId);

            // 2. Obliczamy sumę kosztów
            double totalCost = expenses.stream()
                    .mapToDouble(Expense::getAmount)
                    .sum();

            // 3. Wywołanie serwisu generującego PDF
            byte[] pdfBytes = pdfReportService.generateEventReport(eventId, "Raport Wydarzenia " + eventId, totalCost);

            // 4. Konfiguracja nagłówków
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "raport-wydarzenia-" + eventId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(); // Warto logować błąd, aby łatwiej go naprawić
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}