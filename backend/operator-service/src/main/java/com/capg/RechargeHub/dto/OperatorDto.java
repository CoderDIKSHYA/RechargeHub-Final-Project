package com.capg.RechargeHub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/*
 * DTO for operator details.
 * Used by controller and service layers for data exchange.
 * Validations:
 *   - name: only letters and spaces
 *   - type: only letters and spaces
 *   - circle: only letters and spaces
 */
public class OperatorDto {

    private Long id;

    @NotBlank(message = "Operator name is required")
    @Pattern(
        regexp = "^[a-zA-Z ]+$",
        message = "Operator name must contain only letters and spaces"
    )
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Type is required")
    @Pattern(
        regexp = "^[a-zA-Z ]+$",
        message = "Type must contain only letters and spaces (e.g. Prepaid, Postpaid)"
    )
    private String type;

    @NotBlank(message = "Circle is required")
    @Pattern(
        regexp = "^[a-zA-Z ]+$",
        message = "Circle must contain only letters and spaces"
    )
    private String circle;

    private String logoUrl;
    private List<PlanDto> plans;

    public OperatorDto() {}

    public OperatorDto(Long id, String name, String type, String circle, String logoUrl, List<PlanDto> plans) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.circle = circle;
        this.logoUrl = logoUrl;
        this.plans = plans;
    }

    // 2-arg constructor used in tests
    public OperatorDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCircle() { return circle; }
    public void setCircle(String circle) { this.circle = circle; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public List<PlanDto> getPlans() { return plans; }
    public void setPlans(List<PlanDto> plans) { this.plans = plans; }
}
