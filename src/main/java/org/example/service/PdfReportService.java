package org.example.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfReportService {

    public byte[] generateEventReport(Long eventId, String eventName, double totalCost) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Nagłówek
            document.add(new Paragraph("Raport wydarzenia")
                    .setFontSize(24)
                    .setBold());

            document.add(new Paragraph("Nazwa: " + eventName));
            document.add(new Paragraph("Całkowity koszt: " + totalCost + " PLN"));
            document.add(new Paragraph(" ")); // Przerwa

            // Tabela wydatków
            Table table = new Table(new float[]{3, 2, 2});
            table.addHeaderCell("Opis wydatku");
            table.addHeaderCell("Kwota (PLN)");
            table.addHeaderCell("Płacący");

            // TODO: Pobierz dane z bazy przez osobę #2
            // Na razie przykładowe dane
            table.addCell("Pizza");
            table.addCell("150.00");
            table.addCell("Jan Kowalski");

            table.addCell("Napoje");
            table.addCell("80.00");
            table.addCell("Anna Nowak");

            document.add(table);
            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Błąd generowania PDF: " + e.getMessage());
        }
    }
}