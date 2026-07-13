/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PaymentEvent (payment-service)
 * DESCRIPTION:
 *   Serializable event object published to RabbitMQ after a
 *   payment transaction is processed.
 *   Consumed by the notification-service to create user notifications.
 * ================================================================ */
package com.capg.RechargeHub.dto;

import java.io.Serializable;

public class PaymentEvent implements Serializable {
    private Long transactionId;
    private Long rechargeId;
    private Long userId;
    private String status;
    private String userEmail;
    private java.math.BigDecimal amount;
    private String mobileNumber;
    private String operatorName;

    public PaymentEvent() {}

    public PaymentEvent(Long transactionId, Long rechargeId, Long userId, String status, String userEmail, java.math.BigDecimal amount, String mobileNumber, String operatorName) {
        this.transactionId = transactionId;
        this.rechargeId = rechargeId;
        this.userId = userId;
        this.status = status;
        this.userEmail = userEmail;
        this.amount = amount;
        this.mobileNumber = mobileNumber;
        this.operatorName = operatorName;
    }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Long getRechargeId() { return rechargeId; }
    public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
}


