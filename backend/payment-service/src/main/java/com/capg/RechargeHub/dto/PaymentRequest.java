/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PaymentRequest (payment-service)
 * DESCRIPTION:
 *   DTO for the incoming payment request payload.
 *   Contains recharge ID, user ID, amount, and payment method.
 *   Used by PaymentController to receive and process payment data.
 * ================================================================ */
package com.capg.RechargeHub.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {
    @NotNull(message = "Recharge ID is required")
    private Long rechargeId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Minimum amount is 1.0")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    private String userEmail;
    private String mobileNumber;
    private String operatorName;


    public PaymentRequest() {}

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


