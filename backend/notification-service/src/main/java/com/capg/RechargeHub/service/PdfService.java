package com.capg.RechargeHub.service;

import com.capg.RechargeHub.dto.PaymentEvent;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final Logger logger = LogManager.getLogger(PdfService.class);

    public ByteArrayOutputStream generateReceipt(PaymentEvent event) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font styles
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.BLUE);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("RechargeHub", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Payment Receipt", subHeaderFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Divider
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));

            // Content Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20);
            table.setSpacingAfter(20);

            addTableCell(table, "Transaction ID:", boldFont);
            addTableCell(table, event.getTransactionId().toString(), normalFont);

            addTableCell(table, "Date:", boldFont);
            addTableCell(table, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")), normalFont);

            addTableCell(table, "Mobile Number:", boldFont);
            addTableCell(table, event.getMobileNumber() != null ? event.getMobileNumber() : "N/A", normalFont);

            addTableCell(table, "Operator:", boldFont);
            addTableCell(table, event.getOperatorName() != null ? event.getOperatorName() : "N/A", normalFont);

            addTableCell(table, "Amount Paid:", boldFont);
            addTableCell(table, "INR " + (event.getAmount() != null ? event.getAmount().toString() : "0.00"), normalFont);

            addTableCell(table, "Payment Status:", boldFont);
            addTableCell(table, event.getStatus(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, event.getStatus().equals("SUCCESS") ? Color.GREEN : Color.RED));

            document.add(table);

            // Footer
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
            Paragraph footer = new Paragraph("\nThank you for using RechargeHub!\nThis is a computer-generated receipt and does not require a signature.", 
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            logger.error("Failed to generate PDF receipt: {}", e.getMessage());
        }
        
        return out;
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8);
        table.addCell(cell);
    }
}
