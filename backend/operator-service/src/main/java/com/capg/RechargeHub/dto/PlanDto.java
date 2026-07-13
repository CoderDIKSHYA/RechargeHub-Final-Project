
package com.capg.RechargeHub.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * DTO for plan details sent over API responses.
 * Used by controller and service layers for data exchange.
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDto {

    private Long id;
    private Long operatorId;
    
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private Double amount;
    
    @NotBlank(message = "Validity is required")
    private String validity;
    
    @NotBlank(message = "Description is required")
    private String description;

    private String data;
    private String type;
}
