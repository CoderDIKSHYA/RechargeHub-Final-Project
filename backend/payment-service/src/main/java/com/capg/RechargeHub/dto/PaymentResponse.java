/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PaymentResponse (payment-service)
 * DESCRIPTION:
 *   DTO representing the payment transaction result returned
 *   by the Payment Service API after processing a payment.
 *   Includes transaction ID, status, and timestamp.
 * ================================================================ */
package com.capg.RechargeHub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long rechargeId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime transactionTime;
    private String transactionId;

    public PaymentResponse() {}

    public PaymentResponse(Long id, Long rechargeId, Long userId, BigDecimal amount, String paymentMethod, String status, LocalDateTime transactionTime) {
        this.id = id;
        this.rechargeId = rechargeId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionTime = transactionTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRechargeId() { return rechargeId; }
    public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
