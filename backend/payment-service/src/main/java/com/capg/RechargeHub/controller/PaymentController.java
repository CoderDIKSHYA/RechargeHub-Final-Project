/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PaymentController
 * DESCRIPTION:
 *   REST controller for the Payment Service.
 *   Exposes APIs for processing payments and querying transaction
 *   history by ID, user ID, or recharge ID.
 *   Delegates all business logic to PaymentService.
 *   Swagger annotations are applied for API documentation.
 * ================================================================ */
package com.capg.RechargeHub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capg.RechargeHub.dto.PaymentRequest;
import com.capg.RechargeHub.dto.PaymentResponse;
import com.capg.RechargeHub.service.PaymentService;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LogManager.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    // ✅ GET ALL (Admin)
    @GetMapping("/all")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        logger.info("Admin: Fetching all payments");
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // ✅ POST API
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        logger.info("Received payment request: {}", request);
        PaymentResponse response = paymentService.processPayment(request);
        logger.info("Payment processed successfully. Payment ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ✅ GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        logger.info("Fetching payment with ID: {}", id);
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // ✅ GET by User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId) {
        logger.info("Fetching payments for user ID: {}", userId);
        return ResponseEntity.ok(paymentService.getPaymentsByUserId(userId));
    }

    // ✅ GET by Recharge ID
    @GetMapping("/recharge/{id}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByRechargeId(@PathVariable Long id) {
        logger.info("Fetching payments for recharge ID: {}", id);
        return ResponseEntity.ok(paymentService.getPaymentsByRechargeId(id));
    }
}