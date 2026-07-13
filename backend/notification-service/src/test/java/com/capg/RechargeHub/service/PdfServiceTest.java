package com.capg.RechargeHub.service;

import com.capg.RechargeHub.dto.PaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PdfServiceTest {

    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService();
    }

    @Test
    void testGenerateReceipt_Success() {
        PaymentEvent event = new PaymentEvent();
        event.setTransactionId(1L);
        event.setRechargeId(1L);
        event.setMobileNumber("9876543210");
        event.setOperatorName("Jio");
        event.setAmount(BigDecimal.valueOf(299));
        event.setStatus("SUCCESS");

        ByteArrayOutputStream result = pdfService.generateReceipt(event);
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    void testGenerateReceipt_NullFields_FailedStatus() {
        PaymentEvent event = new PaymentEvent();
        event.setTransactionId(2L);
        event.setMobileNumber(null);
        event.setOperatorName(null);
        event.setAmount(null);
        event.setStatus("FAILED");

        ByteArrayOutputStream result = pdfService.generateReceipt(event);
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    void testGenerateReceipt_ExceptionHandling() {
        // Test null event to trigger NullPointerException which is caught.
        ByteArrayOutputStream result = pdfService.generateReceipt(null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
