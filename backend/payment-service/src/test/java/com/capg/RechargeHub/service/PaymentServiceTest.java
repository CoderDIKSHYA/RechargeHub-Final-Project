/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PaymentServiceTest
 * DESCRIPTION:
 *   Unit test class for PaymentService.
 *   Uses Mockito to mock TransactionRepository and RabbitTemplate.
 *   Tests cover successful payment processing, RabbitMQ failure
 *   resilience, transaction retrieval by ID/userId/rechargeId,
 *   and DB error scenarios.
 *   No Oracle DB, RabbitMQ, or Docker required to run these tests.
 * ================================================================ */
package com.capg.RechargeHub.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.capg.RechargeHub.dto.PaymentRequest;
import com.capg.RechargeHub.dto.PaymentResponse;
import com.capg.RechargeHub.entity.Transaction;
import com.capg.RechargeHub.repository.TransactionRepository;
import com.capg.RechargeHub.service.PaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private com.capg.RechargeHub.client.RechargeClient rechargeClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Transaction buildTransaction() {
        Transaction t = new Transaction();
        t.setId(1L);
        t.setRechargeId(1L);
        t.setUserId(101L);
        t.setAmount(BigDecimal.valueOf(299));
        t.setPaymentMethod("UPI");
        t.setStatus("SUCCESS");
        t.setTransactionTime(LocalDateTime.now());
        return t;
    }

    private PaymentRequest buildRequest() {
        return new PaymentRequest(
                1L, 101L, BigDecimal.valueOf(299), "UPI",
                "user@example.com", "9876543210", "Jio"
        );
    }

    // ✅ TEST 1: processPayment - Success
    @Test
    void testProcessPaymentSuccess() {
        when(transactionRepository.save(any())).thenReturn(buildTransaction());

        PaymentResponse response = paymentService.processPayment(buildRequest());

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        verify(transactionRepository, times(1)).save(any());
    }

    // ❌ TEST: processPayment - Failed Status Branch
    @Test
    void testProcessPaymentFailedStatus() {
        PaymentRequest req = buildRequest();
        req.setPaymentMethod("FAIL");
        
        Transaction t = buildTransaction();
        t.setStatus("FAILED");
        when(transactionRepository.save(any())).thenReturn(t);

        PaymentResponse response = paymentService.processPayment(req);

        assertEquals("FAILED", response.getStatus());
        verify(rechargeClient).updateStatus(any(), eq("FAILED"), any());
    }

    // ❌ TEST 2: processPayment - DB Error
    @Test
    void testProcessPaymentFailure() {
        when(transactionRepository.save(any()))
                .thenThrow(new RuntimeException("DB Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(buildRequest());
        });

        assertEquals("DB Error", exception.getMessage());
    }

    // ✅ TEST 3: getPaymentById - Found
    @Test
    void testGetPaymentById_Found() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(buildTransaction()));

        PaymentResponse response = paymentService.getPaymentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("SUCCESS", response.getStatus());
    }

    // ❌ TEST 4: getPaymentById - Not Found
    @Test
    void testGetPaymentById_NotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.getPaymentById(99L));

        assertEquals("Transaction not found", ex.getMessage());
    }

    // ✅ TEST 5: getPaymentsByUserId
    @Test
    void testGetPaymentsByUserId() {
        when(transactionRepository.findByUserId(101L)).thenReturn(List.of(buildTransaction()));

        List<PaymentResponse> responses = paymentService.getPaymentsByUserId(101L);

        assertEquals(1, responses.size());
        assertEquals(101L, responses.get(0).getUserId());
    }

    // ✅ TEST 6: getPaymentsByRechargeId
    @Test
    void testGetPaymentsByRechargeId() {
        when(transactionRepository.findByRechargeId(1L)).thenReturn(List.of(buildTransaction()));

        List<PaymentResponse> responses = paymentService.getPaymentsByRechargeId(1L);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getRechargeId());
    }

    // ✅ TEST 7: processPayment - Resilience (RabbitMQ Failure)
    @Test
    void testProcessPayment_RabbitMQFailure() {
        when(transactionRepository.save(any())).thenReturn(buildTransaction());
        doThrow(new RuntimeException("RabbitMQ Down")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Should still succeed even if RabbitMQ is down
        PaymentResponse response = paymentService.processPayment(buildRequest());
        assertEquals("SUCCESS", response.getStatus());
    }

    // ✅ TEST 8: processPayment - Resilience (Feign Failure)
    @Test
    void testProcessPayment_FeignFailure() {
        when(transactionRepository.save(any())).thenReturn(buildTransaction());
        doThrow(new RuntimeException("Feign Down")).when(rechargeClient).updateStatus(any(), any(), any());

        // Should still succeed even if Recharge service update fails
        PaymentResponse response = paymentService.processPayment(buildRequest());
        assertEquals("SUCCESS", response.getStatus());
    }

    // ✅ TEST 9: getAllPayments
    @Test
    void testGetAllPayments() {
        when(transactionRepository.findAll()).thenReturn(List.of(buildTransaction()));
        assertEquals(1, paymentService.getAllPayments().size());
    }
}
