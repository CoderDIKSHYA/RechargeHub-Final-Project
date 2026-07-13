package com.capg.RechargeHub.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.capg.RechargeHub.dto.PaymentRequest;
import com.capg.RechargeHub.dto.PaymentResponse;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PaymentClient
 * DESCRIPTION:
 *   OpenFeign client interface for communicating with the
 *   payment-service microservice.
 *   Targets the Docker service name "payment-service" on port 8084.
 *   Used by RechargeService to trigger payment processing after
 *   a recharge record is saved with PENDING status.
 * ================================================================ */
@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {

    /* ================================================================
     * METHOD: processPayment
     * DESCRIPTION:
     *   Calls POST /api/payments on the payment-service to process
     *   a payment transaction. Returns the payment result including
     *   status (SUCCESS/FAILED) used to update the recharge record.
     * ================================================================ */
    @PostMapping("/api/payments")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);
}