/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : NotificationController
 * DESCRIPTION:
 *   REST controller for the Notification Service.
 *   Exposes APIs for querying notifications by ID, user ID,
 *   or fetching all notifications.
 *   Delegates all business logic to NotificationService.
 *   Swagger annotations are applied for API documentation.
 * ================================================================ */
package com.capg.RechargeHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capg.RechargeHub.dto.NotificationResponse;
import com.capg.RechargeHub.service.NotificationService;
import com.capg.RechargeHub.dto.PaymentEvent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;
import java.io.ByteArrayOutputStream;

/*
 * This class handles notification REST APIs.
 * It acts as the controller layer in the microservice architecture.
 * Logging is added to track incoming REST requests and outcomes.
 */
@Tag(name = "Notification APIs", description = "Manage notifications")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LogManager.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.capg.RechargeHub.service.EmailService emailService;

    @Autowired
    private com.capg.RechargeHub.service.PdfService pdfService;

    @Operation(summary = "Get all notifications")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        logger.info("Request received: getAllNotifications");
        List<NotificationResponse> notifications = notificationService.getAllNotifications();
        logger.info("Successfully fetched {} notifications", notifications.size());
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get notification by ID")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        logger.info("Request received: getNotificationById id={}", id);
        try {
            NotificationResponse response = notificationService.getNotificationById(id);
            logger.info("Notification {} fetched successfully", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching notification {}", id, e);
            throw e;
        }
    }

    @Operation(summary = "Get notifications by User ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(@PathVariable Long userId) {
        logger.info("Request received: getNotificationsByUserId userId={}", userId);
        try {
            List<NotificationResponse> response = notificationService.getNotificationsByUserId(userId);
            logger.info("Notifications for user {} fetched successfully, count={}", userId, response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching notifications for user {}", userId, e);
            throw e;
        }
    }

    @Operation(summary = "Broadcast a global notification")
    @PostMapping("/broadcast")
    public ResponseEntity<NotificationResponse> broadcastNotification(@RequestBody com.capg.RechargeHub.dto.NotificationRequest request) {
        logger.info("Request received: broadcastNotification message={}", request.getMessage());
        NotificationResponse response = notificationService.broadcastNotification(request.getMessage(), request.getType());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send OTP Email")
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtpEmail(@RequestBody com.capg.RechargeHub.dto.OtpEmailRequest request) {
        logger.info("Request received: sendOtpEmail for email={}", request.getEmail());
        try {
            String subject = "🔐 Your RechargeHub Verification Code";
            String htmlBody = buildOtpHtmlBody(request.getOtp());
            emailService.sendHtmlEmail(request.getEmail(), subject, htmlBody);
            logger.info("OTP Email sent successfully to {}", request.getEmail());
            return ResponseEntity.ok("OTP sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}", request.getEmail(), e);
            return ResponseEntity.internalServerError().body("Failed to send OTP");
        }
    }

    @Operation(summary = "Generate PDF Receipt")
    @PostMapping("/generate-receipt")
    public ResponseEntity<byte[]> generateReceipt(@RequestBody PaymentEvent event) {
        logger.info("Request received: generateReceipt for transactionId={}", event.getTransactionId());
        try {
            ByteArrayOutputStream out = pdfService.generateReceipt(event);
            byte[] pdfBytes = out.toByteArray();
            logger.info("Successfully generated PDF for transactionId={}", event.getTransactionId());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=RechargeHub_Receipt_" + event.getRechargeId() + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);
        } catch (Exception e) {
            logger.error("Failed to generate receipt PDF for transactionId={}", event.getTransactionId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String buildOtpHtmlBody(String otp) {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f5f5f5;padding:20px'>"
             + "<div style='max-width:500px;margin:auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1)'>"
             + "<div style='background:linear-gradient(135deg, #7c3aed, #06b6d4);padding:24px;text-align:center'>"
             + "<h1 style='color:white;margin:0;font-size:24px'>RechargeHub Verification</h1>"
             + "</div>"
             + "<div style='padding:32px;text-align:center'>"
             + "<p style='font-size:16px;color:#333'>Please use the verification code below to securely authenticate your account.</p>"
             + "<div style='margin:32px 0;padding:16px;background:#f8fafc;border:2px dashed #cbd5e1;border-radius:8px'>"
             + "<h2 style='margin:0;font-size:36px;letter-spacing:8px;color:#0f172a'>" + otp + "</h2>"
             + "</div>"
             + "<p style='font-size:14px;color:#64748b'>This code will expire in 10 minutes. If you did not request this, please ignore this email.</p>"
             + "</div></div></body></html>";
    }
}