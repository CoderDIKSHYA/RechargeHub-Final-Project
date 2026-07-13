/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : UserServiceApplication
 * DESCRIPTION:
 *   Entry point for the User Service microservice.
 *   Bootstraps the Spring Boot application with security,
 *   JPA, and Eureka client auto-configuration.
 *   Handles user registration, login (JWT), and profile retrieval.
 * ================================================================ */
package com.capg.RechargeHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.capg.RechargeHub.client")
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}
