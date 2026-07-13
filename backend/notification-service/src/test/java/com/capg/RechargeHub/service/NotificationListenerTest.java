/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : NotificationListenerTest
 * DESCRIPTION:
 *   Unit tests for NotificationListener.
 *   Covers all branches:
 *     1. SUCCESS + email → HTML email with PDF attachment
 *     2. FAILED  + email → HTML email only (no PDF)
 *     3. SUCCESS + no email → notification saved, no email sent
 *     4. SUCCESS + empty email → notification saved, no email sent
 * ================================================================ */
package com.capg.RechargeHub.service;

import com.capg.RechargeHub.dto.PaymentEvent;
import com.capg.RechargeHub.repository.NotificationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private NotificationListener notificationListener;

    // ✅ TEST 1: SUCCESS + email → saves notification + sends HTML email with PDF
    @Test
    void testConsumePaymentEvent_Success_WithEmail() throws Exception {
        PaymentEvent event = new PaymentEvent(
                1L, 1L, 101L, "SUCCESS",
                "user@example.com", BigDecimal.valueOf(299),
                "9876543210", "Jio"
        );

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        when(pdfService.generateReceipt(event)).thenReturn(pdfStream);

        notificationListener.consumePaymentEvent(event);

        verify(notificationRepository, times(1)).save(any());
        verify(pdfService, times(1)).generateReceipt(event);
        verify(emailService, times(1)).sendHtmlEmailWithAttachment(
                anyString(), anyString(), anyString(), any(), anyString());
        verify(emailService, never()).sendHtmlEmail(any(), any(), any());
    }

    // ✅ TEST 2: FAILED + email → saves notification + sends HTML email (no PDF)
    @Test
    void testConsumePaymentEvent_Failed_WithEmail() throws Exception {
        PaymentEvent event = new PaymentEvent(
                2L, 2L, 102L, "FAILED",
                "user2@example.com", BigDecimal.valueOf(199),
                "9876543211", "Airtel"
        );

        notificationListener.consumePaymentEvent(event);

        verify(notificationRepository, times(1)).save(any());
        verify(emailService, times(1)).sendHtmlEmail(anyString(), anyString(), anyString());
        verify(pdfService, never()).generateReceipt(any());
        verify(emailService, never()).sendHtmlEmailWithAttachment(any(), any(), any(), any(), any());
    }

    // ✅ TEST 3: SUCCESS + no email → saves notification, skips email
    @Test
    void testConsumePaymentEvent_NoEmail() throws Exception {
        PaymentEvent event = new PaymentEvent(
                3L, 3L, 103L, "SUCCESS",
                null, BigDecimal.valueOf(299),
                "9876543212", "Jio"
        );

        notificationListener.consumePaymentEvent(event);

        verify(notificationRepository, times(1)).save(any());
        verify(emailService, never()).sendHtmlEmailWithAttachment(any(), any(), any(), any(), any());
        verify(emailService, never()).sendHtmlEmail(any(), any(), any());
        verify(pdfService, never()).generateReceipt(any());
    }

    // ✅ TEST 4: SUCCESS + empty email → saves notification, skips email
    @Test
    void testConsumePaymentEvent_EmptyEmail() throws Exception {
        PaymentEvent event = new PaymentEvent(
                4L, 4L, 104L, "SUCCESS",
                "", BigDecimal.valueOf(299),
                "9876543213", "Jio"
        );

        notificationListener.consumePaymentEvent(event);

        verify(notificationRepository, times(1)).save(any());
        verify(emailService, never()).sendHtmlEmailWithAttachment(any(), any(), any(), any(), any());
        verify(emailService, never()).sendHtmlEmail(any(), any(), any());
    }
    // ✅ TEST 5: PDF Service failure resilience
    @Test
    void testConsumePaymentEvent_PdfFailure() throws Exception {
        PaymentEvent event = new PaymentEvent(1L, 1L, 101L, "SUCCESS", "u@e.com", BigDecimal.ONE, "99", "O");
        when(pdfService.generateReceipt(any())).thenThrow(new RuntimeException("PDF Fail"));

        // Should not throw exception
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> notificationListener.consumePaymentEvent(event));
        verify(notificationRepository).save(any());
    }

    // ✅ TEST 6: Email Service failure resilience
    @Test
    void testConsumePaymentEvent_EmailFailure() throws Exception {
        PaymentEvent event = new PaymentEvent(1L, 1L, 101L, "FAILED", "u@e.com", BigDecimal.ONE, "99", "O");
        doThrow(new RuntimeException("SMTP Fail")).when(emailService).sendHtmlEmail(any(), any(), any());

        // Should not throw exception
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> notificationListener.consumePaymentEvent(event));
        verify(notificationRepository).save(any());
    }

    // ✅ TEST 7: Null field resilience
    @Test
    void testConsumePaymentEvent_NullFields() {
        PaymentEvent event = new PaymentEvent();
        event.setStatus("SUCCESS");
        event.setAmount(null);
        event.setMobileNumber(null);
        event.setOperatorName(null);
        event.setUserEmail("u@e.com");

        when(pdfService.generateReceipt(any())).thenReturn(new ByteArrayOutputStream());

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> notificationListener.consumePaymentEvent(event));
    }

    // ✅ TEST 8: Null field resilience (Failure Path)
    @Test
    void testConsumePaymentEvent_NullFields_Failure() {
        PaymentEvent event = new PaymentEvent();
        event.setStatus("FAILED");
        event.setAmount(null);
        event.setMobileNumber(null);
        event.setOperatorName(null);
        event.setUserEmail("u@e.com");

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> notificationListener.consumePaymentEvent(event));
    }
}
