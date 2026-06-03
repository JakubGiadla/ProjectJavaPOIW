package com.example.demo.service;

import com.example.demo.dto.EventReportDTO; // Dodaj ten import
import com.example.demo.model.Expense;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfReportService {

    // Usunąłem niepotrzebne wstrzykiwanie ExpenseRepository

    public byte[] generateEventReport(EventReportDTO report) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Raport wydarzenia").setFontSize(24).setBold());
            document.add(new Paragraph("Nazwa: " + report.getEventTitle()));
            document.add(new Paragraph("Całkowity koszt: " + String.format("%.2f", report.getTotalCost()) + " PLN"));
            document.add(new Paragraph(" "));

            Table table = new Table(new float[]{3, 2, 2});
            table.addHeaderCell("Opis wydatku");
            table.addHeaderCell("Kwota (PLN)");
            table.addHeaderCell("Płacący");

            // Dane bierzemy teraz z obiektu report, a nie z repozytorium
            for (Expense exp : report.getExpenses()) {
                table.addCell(exp.getDescription());
                table.addCell(String.format("%.2f", exp.getAmount()));
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