package com.example.demo.controller;

import com.example.demo.dto.EventReportDTO;
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

    public ReportController(PdfReportService pdfReportService, ExpenseRepository expenseRepository) {
        this.pdfReportService = pdfReportService;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long eventId) {
        try {
            // 1. Pobieramy wydatki z bazy dla danego wydarzenia
            List<Expense> expenses = expenseRepository.findByEventId(eventId);

            // 2. Obliczamy sumę kosztów
            double totalCost = expenses.stream()
                    .mapToDouble(Expense::getAmount)
                    .sum();

            // 3. Budujemy obiekt DTO z danymi do raportu
            EventReportDTO report = EventReportDTO.builder()
                    .eventTitle("Raport Wydarzenia " + eventId)
                    .expenses(expenses)
                    .totalCost(totalCost)
                    .build();

            // 4. Wywołujemy serwis generujący PDF przekazując obiekt DTO
            byte[] pdfBytes = pdfReportService.generateEventReport(report);

            // 5. Konfiguracja nagłówków odpowiedzi
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "raport-wydarzenia-" + eventId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}