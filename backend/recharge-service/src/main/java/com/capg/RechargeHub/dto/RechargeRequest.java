package com.capg.RechargeHub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : RechargeRequest
 * DESCRIPTION:
 *   Data Transfer Object (DTO) representing the incoming request
 *   payload for initiating a mobile recharge.
 *   Contains validation constraints to ensure data integrity
 *   before the request reaches the service layer.
 * ================================================================ */
public class RechargeRequest {

    @NotNull(message = "Operator ID cannot be null")
    private Long operatorId;

    @NotNull(message = "Plan ID cannot be null")
    private Long planId;

    @NotBlank(message = "Mobile number cannot be empty")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    private String mobileNumber;

    @NotBlank(message = "Payment method cannot be empty")
    private String paymentMethod;

    /* ================================================================
     * CONSTRUCTOR: RechargeRequest (no-args)
     * DESCRIPTION:
     *   Default constructor required for JSON deserialization.
     * ================================================================ */
    public RechargeRequest() {}

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
