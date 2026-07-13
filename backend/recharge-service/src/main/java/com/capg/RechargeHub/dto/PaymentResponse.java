package com.capg.RechargeHub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PaymentResponse
 * DESCRIPTION:
 *   Data Transfer Object (DTO) received from the payment-service
 *   via the PaymentClient Feign interface.
 *   Contains the transaction result including status (SUCCESS/FAILED)
 *   which is used to update the recharge record accordingly.
 * ================================================================ */
public class PaymentResponse {

    private Long id;
    private Long rechargeId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime transactionTime;
    private String message;
    private String transactionId;

    /* ================================================================
     * CONSTRUCTOR: PaymentResponse (no-args)
     * DESCRIPTION:
     *   Default constructor required for JSON deserialization by Feign.
     * ================================================================ */
    public PaymentResponse() {}

    /* ================================================================
     * CONSTRUCTOR: PaymentResponse (all-args)
     * DESCRIPTION:
     *   Parameterized constructor used in unit tests with Mockito
     *   to create mock payment response objects.
     * ================================================================ */
    public PaymentResponse(Long id, Long rechargeId, Long userId,
                           BigDecimal amount, String paymentMethod,
                           String status, LocalDateTime transactionTime) {
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
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
