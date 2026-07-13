package com.capg.RechargeHub.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.capg.RechargeHub.dto.UserDto;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : UserClient
 * DESCRIPTION:
 *   OpenFeign client interface for communicating with the
 *   user-service microservice.
 *   Targets the Docker service name "user-service" on port 8082.
 *   Used by RechargeService to validate user existence before
 *   processing a recharge request.
 * ================================================================ */
@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    /* ================================================================
     * METHOD: getUserById
     * DESCRIPTION:
     *   Calls GET /users/{id} on the user-service to fetch user
     *   details by user ID. Returns null if the user is not found.
     * ================================================================ */
    @GetMapping("/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}