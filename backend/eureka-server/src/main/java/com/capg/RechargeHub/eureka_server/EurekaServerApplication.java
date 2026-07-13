package com.capg.RechargeHub.eureka_server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/*
 * This class starts the Eureka server microservice.
 * It acts as the entry point (bootstrapping layer) in the microservice
 * architecture and coordinates service registry startup.
 * Logging is added to track server startup and errors.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    private static final Logger logger = LogManager.getLogger(EurekaServerApplication.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting Eureka Server...");
            SpringApplication.run(EurekaServerApplication.class, args);
            logger.info("Eureka Server started successfully");
        } catch (Throwable t) {
            logger.error("Eureka Server failed to start", t);
            throw t;
        }
    }

}
