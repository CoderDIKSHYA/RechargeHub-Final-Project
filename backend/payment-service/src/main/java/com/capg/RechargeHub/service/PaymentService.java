/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PaymentService
 * DESCRIPTION:
 *   Service layer for the Payment Service microservice.
 *   Processes payment transactions, persists them to the database,
 *   and publishes PaymentEvent messages to RabbitMQ for the
 *   notification-service to consume.
 *   RabbitMQ failures are caught and logged as non-critical.
 * ================================================================ */
package com.capg.RechargeHub.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capg.RechargeHub.config.RabbitMQConfig;
import com.capg.RechargeHub.dto.PaymentEvent;
import com.capg.RechargeHub.dto.PaymentRequest;
import com.capg.RechargeHub.dto.PaymentResponse;
import com.capg.RechargeHub.entity.Transaction;
import com.capg.RechargeHub.repository.TransactionRepository;
import com.capg.RechargeHub.client.RechargeClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Service layer for payment processing.
 * Implements payment transaction persistence and RabbitMQ event publishing.
 * Logs key operations and errors at INFO/ERROR levels.
 */
@Service
public class PaymentService {

    private static final Logger logger = LogManager.getLogger(PaymentService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RechargeClient rechargeClient;

    public PaymentResponse processPayment(PaymentRequest request) {
        logger.info("Entering processPayment rechargeId={} userId={}", request.getRechargeId(), request.getUserId());
        try {
            // Logic to process payment
            // For logic coverage, we allow "FAIL" payment method to result in FAILED status
            String status = "FAIL".equalsIgnoreCase(request.getPaymentMethod()) ? "FAILED" : "SUCCESS";

            Transaction transaction = new Transaction();
            transaction.setRechargeId(request.getRechargeId());
            transaction.setUserId(request.getUserId());
            transaction.setAmount(request.getAmount());
            transaction.setPaymentMethod(request.getPaymentMethod());
            transaction.setStatus(status);
            transaction.setTransactionTime(LocalDateTime.now());

            transaction = transactionRepository.save(transaction);
            logger.info("Transaction saved with ID: {}", transaction.getId());

            // Publish event to RabbitMQ
            PaymentEvent event = new PaymentEvent(
                    transaction.getId(),
                    transaction.getRechargeId(),
                    transaction.getUserId(),
                    transaction.getStatus(),
                    request.getUserEmail(),
                    request.getAmount(),
                    request.getMobileNumber(),
                    request.getOperatorName()
            );
            
            try {
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
                logger.info("PaymentEvent published to RabbitMQ for transactionId={}", transaction.getId());
            } catch (Exception e) {
                logger.error("Failed to publish PaymentEvent to RabbitMQ", e);
            }

            // Update recharge-service
            try {
                String statusToUpdate = "SUCCESS".equalsIgnoreCase(transaction.getStatus()) ? "SUCCESS" : "FAILED";
                rechargeClient.updateStatus(transaction.getRechargeId(), statusToUpdate, "RH-" + transaction.getId());
                logger.info("Recharge status updated to {} for rechargeId={}", statusToUpdate, transaction.getRechargeId());
            } catch (Exception e) {
                logger.error("Failed to update recharge status for rechargeId={}", transaction.getRechargeId(), e);
            }

            return mapToResponse(transaction);
        } catch (Exception e) {
            logger.error("Error in processPayment", e);
            throw e;
        }
    }

    public List<PaymentResponse> getAllPayments() {
        logger.info("Fetching all payments (Admin)");
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return mapToResponse(transaction);
    }

    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<PaymentResponse> getPaymentsByRechargeId(Long rechargeId) {
        return transactionRepository.findByRechargeId(rechargeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Transaction transaction) {
        PaymentResponse response = new PaymentResponse();
        response.setId(transaction.getId());
        response.setRechargeId(transaction.getRechargeId());
        response.setUserId(transaction.getUserId());
        response.setAmount(transaction.getAmount());
        response.setPaymentMethod(transaction.getPaymentMethod());
        response.setStatus(transaction.getStatus());
        response.setTransactionTime(transaction.getTransactionTime());
        response.setTransactionId("RH-" + transaction.getId());
        return response;
    }
}
