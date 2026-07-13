package com.capg.RechargeHub.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.capg.RechargeHub.service.PaymentService;
import com.capg.RechargeHub.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/payments/razorpay")
public class RazorpayController {

    @Value("${RAZORPAY_KEY_ID}")
    private String keyId;

    @Value("${RAZORPAY_KEY_SECRET}")
    private String keySecret;

    @Autowired
    private PaymentService paymentService;

    @PostMapping({"/create-order", "/create_order"})
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {
        try {
            System.out.println("Processing Razorpay order creation request: " + data);
            
            if (data.get("amount") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Amount is required"));
            }

            double amount = Double.parseDouble(data.get("amount").toString());
            // Razorpay expects amount in paise (multiply by 100)
            int amountInPaise = (int) (amount * 100);

            System.out.println("Initializing RazorpayClient with Key ID: " + keyId);
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            org.json.JSONObject options = new org.json.JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", "txn_" + UUID.randomUUID().toString().substring(0, 8));

            System.out.println("Creating order with options: " + options);
            Order order = client.orders.create(options);
            
            Map<String, String> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", String.valueOf(amountInPaise));
            response.put("currency", "INR");
            response.put("keyId", keyId);

            System.out.println("Successfully created Razorpay order: " + order.get("id"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to create Razorpay order");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error creating Razorpay order: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-signature")
    public ResponseEntity<?> verifySignature(@RequestBody Map<String, String> paymentData) {
        try {
            String orderId = paymentData.get("razorpay_order_id");
            String paymentId = paymentData.get("razorpay_payment_id");
            String signature = paymentData.get("razorpay_signature");

            String payload = orderId + "|" + paymentId;
            boolean isValid = Utils.verifySignature(payload, signature, keySecret);

            if (isValid) {
                // If signature is valid, process payment in our DB and trigger events
                PaymentRequest req = new PaymentRequest();
                req.setRechargeId(Long.parseLong(paymentData.get("rechargeId")));
                req.setUserId(Long.parseLong(paymentData.get("userId")));
                req.setAmount(new BigDecimal(paymentData.get("amount")));
                req.setPaymentMethod("RAZORPAY");
                req.setUserEmail(paymentData.get("email"));
                req.setMobileNumber(paymentData.get("mobile"));
                req.setOperatorName(paymentData.get("operator"));
                
                paymentService.processPayment(req);
                
                return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Payment verified and processed."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", "FAILED", "error", "Invalid payment signature."));
            }
        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "FAILED", "error", e.getMessage()));
        }
    }
}
