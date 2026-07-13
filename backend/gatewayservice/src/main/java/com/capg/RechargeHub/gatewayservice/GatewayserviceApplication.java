/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : GatewayserviceApplication
 * DESCRIPTION:
 *   Entry point for the API Gateway Service microservice.
 *   Bootstraps the Spring Cloud Gateway with reactive WebFlux,
 *   Eureka client for service discovery, and load-balanced routing.
 *   Routes incoming requests to downstream microservices by path.
 * ================================================================ */
package com.capg.RechargeHub.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayserviceApplication.class, args);
    }
}