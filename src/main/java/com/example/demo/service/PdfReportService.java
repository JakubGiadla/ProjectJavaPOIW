package com.example.demo.service;

import com.example.demo.model.Expense;
import com.example.demo.repository.ExpenseRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfReportService {

    private final ExpenseRepository expenseRepository;

    // Wstrzykujemy repozytorium przez konstruktor
    public PdfReportService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public byte[] generateEventReport(Long eventId, String eventName, double totalCost) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Raport wydarzenia").setFontSize(24).setBold());
            document.add(new Paragraph("Nazwa: " + eventName));
            document.add(new Paragraph("Całkowity koszt: " + String.format("%.2f", totalCost) + " PLN"));
            document.add(new Paragraph(" "));

            Table table = new Table(new float[]{3, 2, 2});
            table.addHeaderCell("Opis wydatku");
            table.addHeaderCell("Kwota (PLN)");
            table.addHeaderCell("Płacący");

            // POBIERANIE DANYCH Z BAZY
            // Pobieramy wszystkie wydatki (możesz później dodać metodę findByEventId w repozytorium)
            List<Expense> expenses = expenseRepository.findAll();

            for (Expense exp : expenses) {
                table.addCell(exp.getDescription());
                table.addCell(String.format("%.2f", exp.getAmount()));
                // Używamy getName() zamiast dostępu do pola, żeby uniknąć błędu "private access"
                table.addCell(exp.getPayer() != null ? exp.getPayer().getName() : "Nieznany");
            }

            document.add(table);
            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Błąd generowania PDF: " + e.getMessage());
        }
    }
}