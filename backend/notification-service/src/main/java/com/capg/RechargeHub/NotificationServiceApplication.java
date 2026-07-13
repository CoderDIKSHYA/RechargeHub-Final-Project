/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : NotificationServiceApplication
 * DESCRIPTION:
 *   Entry point for the Notification Service microservice.
 *   Bootstraps the Spring Boot application with JPA, RabbitMQ AMQP,
 *   and Eureka client auto-configuration.
 *   Listens to payment events from RabbitMQ and persists notifications.
 * ================================================================ */
package com.capg.RechargeHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
