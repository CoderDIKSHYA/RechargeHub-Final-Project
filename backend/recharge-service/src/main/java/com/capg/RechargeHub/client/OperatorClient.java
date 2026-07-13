package com.capg.RechargeHub.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.capg.RechargeHub.dto.OperatorDto;
import com.capg.RechargeHub.dto.PlanDto;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : OperatorClient
 * DESCRIPTION:
 *   OpenFeign client interface for communicating with the
 *   operator-service microservice.
 *   Targets the Docker service name "operator-service" on port 8083.
 *   Used by RechargeService to validate operator and plan details
 *   before processing a recharge request.
 * ================================================================ */
@FeignClient(name = "OPERATOR-SERVICE")
public interface OperatorClient {

    /* ================================================================
     * METHOD: getOperatorById
     * DESCRIPTION:
     *   Calls GET /operators/{id} on the operator-service to fetch
     *   operator details by operator ID.
     * ================================================================ */
    @GetMapping("/operators/{id}")
    OperatorDto getOperatorById(@PathVariable("id") Long id);

    /* ================================================================
     * METHOD: getPlanById
     * DESCRIPTION:
     *   Calls GET /plans/{id} on the operator-service to fetch
     *   plan details by plan ID. Used to validate plan-operator
     *   association and retrieve the recharge amount.
     * ================================================================ */
    @GetMapping("/operators/plans/{id}")
    PlanDto getPlanById(@PathVariable("id") Long id);
}