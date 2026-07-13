/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PaymentControllerTest
 * DESCRIPTION:
 *   Unit test class for PaymentController.
 *   Uses MockMvc standalone setup to test HTTP layer without
 *   loading the full Spring context or requiring any infrastructure.
 *   Mocks PaymentService to isolate controller behavior.
 *   Tests cover POST /api/payments, GET /api/payments/{id},
 *   GET /api/payments/user/{userId}, GET /api/payments/recharge/{id}.
 * ================================================================ */
package com.capg.RechargeHub.controller;

import com.capg.RechargeHub.dto.PaymentRequest;
import com.capg.RechargeHub.dto.PaymentResponse;
import com.capg.RechargeHub.service.PaymentService;
import com.capg.RechargeHub.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private PaymentResponse buildResponse() {
        return new PaymentResponse(
                1L, 1L, 101L, BigDecimal.valueOf(299),
                "UPI", "SUCCESS", LocalDateTime.now()
        );
    }

    private PaymentRequest buildRequest() {
        return new PaymentRequest(
                1L, 101L, BigDecimal.valueOf(299), "UPI",
                "user@example.com", "9876543210", "Jio"
        );
    }

    // ✅ TEST: POST /api/payments - Success
    @Test
    void testProcessPaymentSuccess() throws Exception {
        when(paymentService.processPayment(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    // ❌ TEST: POST /api/payments - Failure
    @Test
    void testProcessPaymentFailure() throws Exception {
        when(paymentService.processPayment(any()))
                .thenThrow(new RuntimeException("Payment Failed"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Payment Failed"));
    }

    // ✅ TEST: GET /api/payments/{id} - Found
    @Test
    void testGetPaymentById_Returns200() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    // ❌ TEST: GET /api/payments/{id} - Not Found
    @Test
    void testGetPaymentById_NotFound() throws Exception {
        when(paymentService.getPaymentById(99L))
                .thenThrow(new RuntimeException("Transaction not found"));

        mockMvc.perform(get("/api/payments/99"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Transaction not found"));
    }

    // ✅ TEST: GET /api/payments/user/{userId}
    @Test
    void testGetPaymentsByUserId_Returns200() throws Exception {
        when(paymentService.getPaymentsByUserId(101L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/payments/user/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(101));
    }

    // ❌ TEST: GET /api/payments/user/{userId} - Failure
    @Test
    void testGetPaymentsByUserId_Failure() throws Exception {
        when(paymentService.getPaymentsByUserId(101L))
                .thenThrow(new RuntimeException("User error"));

        mockMvc.perform(get("/api/payments/user/101"))
                .andExpect(status().isInternalServerError());
    }

    // ✅ TEST: GET /api/payments/recharge/{id}
    @Test
    void testGetPaymentsByRechargeId_Returns200() throws Exception {
        when(paymentService.getPaymentsByRechargeId(1L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/payments/recharge/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rechargeId").value(1));
    }

    // ❌ TEST: GET /api/payments/recharge/{id} - Failure
    @Test
    void testGetPaymentsByRechargeId_Failure() throws Exception {
        when(paymentService.getPaymentsByRechargeId(1L))
                .thenThrow(new RuntimeException("Recharge error"));

        mockMvc.perform(get("/api/payments/recharge/1"))
                .andExpect(status().isInternalServerError());
    }

    // ✅ TEST: GET /api/payments/all (Admin)
    @Test
    void testGetAllPayments_Returns200() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/payments/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
    
    // ❌ TEST: GET /api/payments/all - Failure
    @Test
    void testGetAllPayments_Failure() throws Exception {
        when(paymentService.getAllPayments())
                .thenThrow(new RuntimeException("Admin error"));

        mockMvc.perform(get("/api/payments/all"))
                .andExpect(status().isInternalServerError());
    }
}
