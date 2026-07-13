package com.capg.RechargeHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : RechargeServiceApplication
 * DESCRIPTION:
 *   Entry point for the Recharge Service microservice.
 *   Bootstraps the Spring Boot application and enables
 *   OpenFeign clients for inter-service communication with
 *   user-service, operator-service, and payment-service.
 * ================================================================ */
@SpringBootApplication
@EnableFeignClients
public class RechargeServiceApplication {

    /* ================================================================
     * METHOD: main
     * DESCRIPTION:
     *   Launches the Recharge Service Spring Boot application.
     *   Prints a startup confirmation message to the console.
     * ================================================================ */
    public static void main(String[] args) {
        SpringApplication.run(RechargeServiceApplication.class, args);
    }
}
