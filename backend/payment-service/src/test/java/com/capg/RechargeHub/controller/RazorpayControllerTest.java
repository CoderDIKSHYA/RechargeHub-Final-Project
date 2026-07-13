/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : RazorpayControllerTest
 * DESCRIPTION:
 *   Unit test class for RazorpayController.
 *   Uses Mockito to mock PaymentService and Mockito.mockConstruction
 *   to handle the manual instantiation of RazorpayClient.
 * ================================================================ */
package com.capg.RechargeHub.controller;

import com.capg.RechargeHub.service.PaymentService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class RazorpayControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private RazorpayController razorpayController;

    private MockedConstruction<RazorpayClient> mockedRazorpayClient;

    @BeforeEach
    void setUp() throws RazorpayException {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(razorpayController).build();
        
        // Set values for @Value fields
        ReflectionTestUtils.setField(razorpayController, "keyId", "test_key");
        ReflectionTestUtils.setField(razorpayController, "keySecret", "test_secret");

        // Mock RazorpayClient construction
        mockedRazorpayClient = mockConstruction(RazorpayClient.class, (mock, context) -> {
            // Mock the nested structures
            mock.orders = mock(com.razorpay.OrderClient.class);
            
            // Mock order creation
            com.razorpay.Order order = mock(com.razorpay.Order.class);
            when(order.get("id")).thenReturn("order_123");
            when(mock.orders.create(any(JSONObject.class))).thenReturn(order);
        });
    }

    @AfterEach
    void tearDown() {
        if (mockedRazorpayClient != null) {
            mockedRazorpayClient.close();
        }
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", 500.0);

        mockMvc.perform(post("/api/payments/razorpay/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 500.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order_123"))
                .andExpect(jsonPath("$.amount").value("50000"));
    }

    @Test
    void testCreateOrder_RazorpayException() throws Exception {
        // Close and recreate with exception
        mockedRazorpayClient.close();
        mockedRazorpayClient = mockConstruction(RazorpayClient.class, (mock, context) -> {
            mock.orders = mock(com.razorpay.OrderClient.class);
            when(mock.orders.create(any(JSONObject.class))).thenThrow(new RazorpayException("Razorpay Error"));
        });

        mockMvc.perform(post("/api/payments/razorpay/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 500.0}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testVerifySignature_Success() throws Exception {
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("razorpay_order_id", "order_123");
        paymentData.put("razorpay_payment_id", "pay_123");
        paymentData.put("razorpay_signature", "sig_123");
        paymentData.put("rechargeId", "1");
        paymentData.put("userId", "101");
        paymentData.put("amount", "500");
        paymentData.put("email", "test@test.com");
        paymentData.put("mobile", "9999999999");
        paymentData.put("operator", "Jio");

        // Mock static verifySignature
        try (var mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.verifySignature(anyString(), anyString(), anyString())).thenReturn(true);

            mockMvc.perform(post("/api/payments/razorpay/verify-signature")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new JSONObject(paymentData).toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"));

            verify(paymentService, times(1)).processPayment(any());
        }
    }

    @Test
    void testVerifySignature_Invalid() throws Exception {
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("razorpay_order_id", "order_123");
        paymentData.put("razorpay_payment_id", "pay_123");
        paymentData.put("razorpay_signature", "sig_123");

        try (var mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.verifySignature(anyString(), anyString(), anyString())).thenReturn(false);

            mockMvc.perform(post("/api/payments/razorpay/verify-signature")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new JSONObject(paymentData).toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("FAILED"));
        }
    }

    @Test
    void testVerifySignature_Exception() throws Exception {
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("razorpay_order_id", "order_123");
        paymentData.put("razorpay_payment_id", "pay_123");
        paymentData.put("razorpay_signature", "sig_123");

        try (var mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.verifySignature(anyString(), anyString(), anyString()))
                    .thenThrow(new RazorpayException("Verification Error"));

            mockMvc.perform(post("/api/payments/razorpay/verify-signature")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new JSONObject(paymentData).toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("FAILED"));
        }
    }
}
