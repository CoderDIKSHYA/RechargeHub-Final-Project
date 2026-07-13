package com.capg.RechargeHub.dto;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : OperatorDto
 * DESCRIPTION:
 *   Data Transfer Object (DTO) used by the OperatorClient Feign
 *   interface to deserialize operator data from operator-service.
 *   Represents the operator details returned by GET /operators/{id}.
 * ================================================================ */
public class OperatorDto {

    private Long id;
    private String name;

    /* ================================================================
     * CONSTRUCTOR: OperatorDto (no-args)
     * DESCRIPTION:
     *   Default constructor required for JSON deserialization by Feign.
     * ================================================================ */
    public OperatorDto() {}

    /* ================================================================
     * CONSTRUCTOR: OperatorDto (all-args)
     * DESCRIPTION:
     *   Parameterized constructor used in unit tests with Mockito
     *   to create mock operator objects without a running service.
     * ================================================================ */
    public OperatorDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
