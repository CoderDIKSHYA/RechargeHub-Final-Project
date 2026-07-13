package com.capg.RechargeHub.gatewayservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/user")
    public ResponseEntity<String> userServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("User Service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/operator")
    public ResponseEntity<String> operatorServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Operator Service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/recharge")
    public ResponseEntity<String> rechargeServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Recharge Service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/payment")
    public ResponseEntity<String> paymentServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Payment Service is currently unavailable. Please try again later.");
    }
}
