package com.capg.RechargeHub.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : PlanDto
 * DESCRIPTION:
 *   Data Transfer Object (DTO) used by the OperatorClient Feign
 *   interface to deserialize plan data from operator-service.
 *   Represents the plan details returned by GET /plans/{id}.
 *   Includes the recharge amount used for payment processing.
 * ================================================================ */
public class PlanDto {

    private Long id;

    @NotNull(message = "Operator ID cannot be null")
    private Long operatorId;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Validity cannot be empty")
    private String validity;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    /* ================================================================
     * CONSTRUCTOR: PlanDto (no-args)
     * DESCRIPTION:
     *   Default constructor required for JSON deserialization by Feign.
     * ================================================================ */
    public PlanDto() {}

    /* ================================================================
     * CONSTRUCTOR: PlanDto (all-args)
     * DESCRIPTION:
     *   Parameterized constructor used in unit tests with Mockito
     *   to create mock plan objects without a running service.
     * ================================================================ */
    public PlanDto(Long id, Long operatorId, BigDecimal amount,
                   String validity, String description) {
        this.id = id;
        this.operatorId = operatorId;
        this.amount = amount;
        this.validity = validity;
        this.description = description;
    }

    public Long getId() { return id; }
    public Long getOperatorId() { return operatorId; }
    public BigDecimal getAmount() { return amount; }
    public String getValidity() { return validity; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setValidity(String validity) { this.validity = validity; }
    public void setDescription(String description) { this.description = description; }
}
