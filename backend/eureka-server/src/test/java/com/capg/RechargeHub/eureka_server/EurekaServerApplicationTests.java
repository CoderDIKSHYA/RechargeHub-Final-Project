package com.capg.RechargeHub.eureka_server;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Context load test disabled — Eureka server requires network setup
@SpringBootTest
class EurekaServerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testMain() {
        // Run main method with mock args to cover startup logic
        EurekaServerApplication.main(new String[]{"--server.port=0"});
    }

    @Test
    void testMainFailure() {
        try (var mockedSpringApplication = org.mockito.Mockito.mockStatic(org.springframework.boot.SpringApplication.class)) {
            mockedSpringApplication.when(() -> org.springframework.boot.SpringApplication.run(
                    org.mockito.ArgumentMatchers.any(Class.class), 
                    org.mockito.ArgumentMatchers.any(String[].class)))
                .thenThrow(new RuntimeException("Startup Failed"));

            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
                EurekaServerApplication.main(new String[]{});
            });
        }
    }
}
