package com.capg.RechargeHub.gatewayservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(FallbackController.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testUserServiceFallback() {
        webTestClient.get().uri("/fallback/user")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class).isEqualTo("User Service is currently unavailable. Please try again later.");
    }

    @Test
    void testOperatorServiceFallback() {
        webTestClient.get().uri("/fallback/operator")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class).isEqualTo("Operator Service is currently unavailable. Please try again later.");
    }

    @Test
    void testRechargeServiceFallback() {
        webTestClient.get().uri("/fallback/recharge")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class).isEqualTo("Recharge Service is currently unavailable. Please try again later.");
    }

    @Test
    void testPaymentServiceFallback() {
        webTestClient.get().uri("/fallback/payment")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class).isEqualTo("Payment Service is currently unavailable. Please try again later.");
    }
}
