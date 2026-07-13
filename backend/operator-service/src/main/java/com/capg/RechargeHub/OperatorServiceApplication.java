/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : OperatorServiceApplication
 * DESCRIPTION:
 *   Entry point for the Operator Service microservice.
 *   Bootstraps the Spring Boot application with JPA and
 *   Eureka client auto-configuration.
 *   Manages operators and recharge plans for the RechargeNova system.
 * ================================================================ */
package com.capg.RechargeHub;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OperatorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperatorServiceApplication.class, args);
    }
}
