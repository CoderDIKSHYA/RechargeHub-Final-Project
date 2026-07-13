/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : NotificationControllerTest
 * DESCRIPTION:
 *   Unit test class for NotificationController.
 *   Uses MockMvc standalone setup to test HTTP layer without
 *   loading the full Spring context or requiring Oracle/RabbitMQ.
 *   Mocks NotificationService to isolate controller behavior.
 *   Tests cover GET /api/notifications, GET /api/notifications/{id},
 *   and GET /api/notifications/user/{userId} endpoints.
 * ================================================================ */
package com.capg.RechargeHub.controller;

import com.capg.RechargeHub.dto.NotificationResponse;
import com.capg.RechargeHub.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
    private com.capg.RechargeHub.service.EmailService emailService;

    @Mock
    private com.capg.RechargeHub.service.PdfService pdfService;

    @InjectMocks
    private NotificationController notificationController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private NotificationResponse buildNotification() {
        return new NotificationResponse(1L, 101L, "Recharge of Rs.299 successful", "EMAIL", "SENT", LocalDateTime.now());
    }

    // ✅ GET /api/notifications
    @Test
    void testGetAllNotifications_Returns200() throws Exception {
        when(notificationService.getAllNotifications()).thenReturn(List.of(buildNotification()));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].message").value("Recharge of Rs.299 successful"));
    }

    // ✅ GET /api/notifications/{id} - Found
    @Test
    void testGetNotificationById_Returns200() throws Exception {
        when(notificationService.getNotificationById(1L)).thenReturn(buildNotification());

        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    // ❌ GET /api/notifications/{id} - Not Found
    @Test
    void testGetNotificationById_NotFound() throws Exception {
        when(notificationService.getNotificationById(99L))
                .thenThrow(new RuntimeException("Notification not found"));

        // Standalone setup rethrows uncaught exceptions
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/notifications/99"));
        });
    }

    // ✅ GET /api/notifications/user/{userId}
    @Test
    void testGetNotificationsByUserId_Returns200() throws Exception {
        when(notificationService.getNotificationsByUserId(101L))
                .thenReturn(List.of(buildNotification()));

        mockMvc.perform(get("/api/notifications/user/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(101));
    }

    @Test
    void testGetNotificationsByUserId_Failure() throws Exception {
        when(notificationService.getNotificationsByUserId(anyLong()))
                .thenThrow(new RuntimeException("DB Error"));

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/notifications/user/101"));
        });
    }

    @Test
    void testGetAllNotifications_Failure() throws Exception {
        when(notificationService.getAllNotifications())
                .thenThrow(new RuntimeException("DB Error"));

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/notifications"));
        });
    }

    // ✅ POST /api/notifications/send-otp
    @Test
    void testSendOtpEmail_Success() throws Exception {
        com.capg.RechargeHub.dto.OtpEmailRequest request = new com.capg.RechargeHub.dto.OtpEmailRequest();
        request.setEmail("u@e.com");
        request.setOtp("123456");

        mockMvc.perform(post("/api/notifications/send-otp")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent successfully"));

        verify(emailService, times(1)).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    // ❌ POST /api/notifications/send-otp - Failure
    @Test
    void testSendOtpEmail_Failure() throws Exception {
        com.capg.RechargeHub.dto.OtpEmailRequest request = new com.capg.RechargeHub.dto.OtpEmailRequest();
        request.setEmail("u@e.com");
        request.setOtp("123456");

        doThrow(new RuntimeException("SMTP Error")).when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/notifications/send-otp")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to send OTP"));
    }

    // ✅ POST /api/notifications/generate-receipt
    @Test
    void testGenerateReceipt_Success() throws Exception {
        com.capg.RechargeHub.dto.PaymentEvent event = new com.capg.RechargeHub.dto.PaymentEvent();
        event.setTransactionId(1L);
        event.setRechargeId(1L);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        out.write("PDF Content".getBytes());
        when(pdfService.generateReceipt(any())).thenReturn(out);

        mockMvc.perform(post("/api/notifications/generate-receipt")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=RechargeHub_Receipt_1.pdf"));
    }

    // ❌ POST /api/notifications/generate-receipt - Failure
    @Test
    void testGenerateReceipt_Failure() throws Exception {
        com.capg.RechargeHub.dto.PaymentEvent event = new com.capg.RechargeHub.dto.PaymentEvent();
        when(pdfService.generateReceipt(any())).thenThrow(new RuntimeException("PDF Error"));

        mockMvc.perform(post("/api/notifications/generate-receipt")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isInternalServerError());
    }
}
