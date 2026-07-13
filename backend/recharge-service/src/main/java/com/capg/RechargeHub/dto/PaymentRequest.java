package com.capg.RechargeHub.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PaymentRequest
 * DESCRIPTION:
 *   Data Transfer Object (DTO) sent to the payment-service via
 *   the PaymentClient Feign interface.
 *   Contains all fields required to initiate a payment transaction
 *   including recharge ID, user ID, amount, and payment method.
 * ================================================================ */
public class PaymentRequest {

    @NotNull(message = "Recharge ID cannot be null")
    private Long rechargeId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Payment method cannot be empty")
    private String paymentMethod;

    private String userEmail;
    private String mobileNumber;
    private String operatorName;

    /* ================================================================
     * CONSTRUCTOR: PaymentRequest (no-args)
     * DESCRIPTION:
     *   Default constructor required for JSON serialization by Feign.
     * ================================================================ */
    public PaymentRequest() {}

    /* ================================================================
     * CONSTRUCTOR: PaymentRequest (all-args)
     * DESCRIPTION:
     *   Parameterized constructor used in unit tests with Mockito
     *   and in the service layer to build the payment request object.
     * ================================================================ */
    public PaymentRequest(Long rechargeId, Long userId, BigDecimal amount, String paymentMethod, String userEmail, String mobileNumber, String operatorName) {
        this.rechargeId = rechargeId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.userEmail = userEmail;
        this.mobileNumber = mobileNumber;
        this.operatorName = operatorName;
    }

    public Long getRechargeId() { return rechargeId; }
    public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
}


