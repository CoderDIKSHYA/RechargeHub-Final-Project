/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : NotificationListener
 * DESCRIPTION:
 *   RabbitMQ message listener for the Notification Service.
 *   Consumes PaymentEvent messages from the payment_queue.
 *
 *   On SUCCESS:
 *     - Saves Notification record (type=EMAIL, status=SENT)
 *     - Generates a PDF receipt via PdfService
 *     - Sends HTML email with PDF attachment via EmailService
 *
 *   On FAILED:
 *     - Saves Notification record (type=EMAIL, status=FAILED)
 *     - Sends plain HTML failure email (no PDF)
 *
 *   If no email present:
 *     - Saves Notification record only, skips email
 * ================================================================ */
package com.capg.RechargeHub.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capg.RechargeHub.config.RabbitMQConfig;
import com.capg.RechargeHub.dto.PaymentEvent;
import com.capg.RechargeHub.entity.Notification;
import com.capg.RechargeHub.repository.NotificationRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

@Service
public class NotificationListener {

    private static final Logger logger = LogManager.getLogger(NotificationListener.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PdfService pdfService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumePaymentEvent(PaymentEvent event) {
        logger.info("Received PaymentEvent: rechargeId={} userId={} status={}",
                event.getRechargeId(), event.getUserId(), event.getStatus());

        boolean isSuccess = "SUCCESS".equalsIgnoreCase(event.getStatus());

        // ── Build notification message ────────────────────────────────────────
        String message = buildMessage(event, isSuccess);

        // ── Persist notification record ───────────────────────────────────────
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setMessage(message);
        notification.setType("EMAIL");
        notification.setStatus(isSuccess ? "SENT" : "FAILED");
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        logger.info("Notification saved for userId={} status={}", event.getUserId(), notification.getStatus());

        // ── Send email ────────────────────────────────────────────────────────
        if (event.getUserEmail() != null && !event.getUserEmail().isEmpty()) {
            logger.info("Attempting to send {} email to: {}", isSuccess ? "SUCCESS" : "FAILURE", event.getUserEmail());
            if (isSuccess) {
                sendSuccessEmailWithPdf(event, message);
            } else {
                sendFailureEmail(event, message);
            }
        } else {
            logger.warn("No email address found for userId={}. Skipping SMTP dispatch.", event.getUserId());
        }
    }

    // ── SUCCESS: HTML email + PDF receipt attachment ──────────────────────────
    private void sendSuccessEmailWithPdf(PaymentEvent event, String plainMessage) {
        try {
            String subject = "✅ RechargeHub: Recharge Successful — Receipt Attached";
            String htmlBody = buildSuccessHtmlBody(event);

            ByteArrayOutputStream pdf = pdfService.generateReceipt(event);
            String fileName = "RechargeHub_Receipt_" + event.getRechargeId() + ".pdf";

            emailService.sendHtmlEmailWithAttachment(
                    event.getUserEmail(), subject, htmlBody, pdf, fileName);

            logger.info("Success email with PDF sent to {}", event.getUserEmail());
        } catch (Exception e) {
            logger.error("Failed to send success email to {}: {}", event.getUserEmail(), e.getMessage());
        }
    }

    // ── FAILURE: HTML email only (no PDF) ────────────────────────────────────
    private void sendFailureEmail(PaymentEvent event, String plainMessage) {
        try {
            String subject = "❌ RechargeHub: Recharge Failed";
            String htmlBody = buildFailureHtmlBody(event);

            emailService.sendHtmlEmail(event.getUserEmail(), subject, htmlBody);

            logger.info("Failure email sent to {}", event.getUserEmail());
        } catch (Exception e) {
            logger.error("Failed to send failure email to {}: {}", event.getUserEmail(), e.getMessage());
        }
    }

    // ── Plain text message (stored in DB) ────────────────────────────────────
    private String buildMessage(PaymentEvent event, boolean isSuccess) {
        if (isSuccess) {
            return String.format(
                "Recharge of INR %.2f for mobile %s via %s was SUCCESSFUL. Transaction ID: %d.",
                event.getAmount() != null ? event.getAmount().doubleValue() : 0.0,
                event.getMobileNumber() != null ? event.getMobileNumber() : "N/A",
                event.getOperatorName() != null ? event.getOperatorName() : "N/A",
                event.getTransactionId()
            );
        } else {
            return String.format(
                "Recharge of INR %.2f for mobile %s via %s FAILED. Please try again.",
                event.getAmount() != null ? event.getAmount().doubleValue() : 0.0,
                event.getMobileNumber() != null ? event.getMobileNumber() : "N/A",
                event.getOperatorName() != null ? event.getOperatorName() : "N/A"
            );
        }
    }

    // ── HTML body for SUCCESS email ───────────────────────────────────────────
    private String buildSuccessHtmlBody(PaymentEvent event) {
        return "<!DOCTYPE html><html><body style='font-family:\"Segoe UI\",Tahoma,sans-serif;background:#0F1113;color:#F1F5F9;padding:40px'>"
            + "<div style='max-width:600px;margin:auto;background:#181A1C;border:1px solid rgba(225,202,150,0.2);border-radius:24px;overflow:hidden;box-shadow:0 20px 40px rgba(0,0,0,0.4)'>"
            + "<div style='background:linear-gradient(135deg, #E1CA96, #B89B5E);padding:40px;text-align:center'>"
            + "<h1 style='color:#121416;margin:0;font-size:28px;font-weight:800;letter-spacing:-1px'>RechargeHub Success</h1>"
            + "</div>"
            + "<div style='padding:40px'>"
            + "<p style='font-size:18px;color:#E1CA96;font-weight:600'>Confirmation of Activation</p>"
            + "<p style='color:#94A3B8;line-height:1.6'>Your premium recharge has been processed successfully. Your connection is now active with the selected plan.</p>"
            + "<div style='background:rgba(225,202,150,0.05);border-radius:16px;padding:24px;margin:32px 0;border:1px solid rgba(225,202,150,0.1)'>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + "<tr><td style='padding:8px 0;color:#94A3B8;font-size:13px'>Transaction ID</td><td style='padding:8px 0;text-align:right;font-weight:700;color:#F1F5F9'>#RH-" + event.getTransactionId() + "</td></tr>"
            + "<tr><td style='padding:8px 0;color:#94A3B8;font-size:13px'>Mobile Number</td><td style='padding:8px 0;text-align:right;font-weight:700;color:#F1F5F9'>" + nvl(event.getMobileNumber()) + "</td></tr>"
            + "<tr><td style='padding:8px 0;color:#94A3B8;font-size:13px'>Operator</td><td style='padding:8px 0;text-align:right;font-weight:700;color:#F1F5F9'>" + nvl(event.getOperatorName()) + "</td></tr>"
            + "<tr><td style='padding:16px 0 0;color:#94A3B8;font-size:13px'>Amount Paid</td><td style='padding:16px 0 0;text-align:right;font-size:24px;font-weight:800;color:#E1CA96'>₹" + (event.getAmount() != null ? event.getAmount().toString() : "0.00") + "</td></tr>"
            + "</table>"
            + "</div>"
            + "<p style='text-align:center;color:#64748B;font-size:13px'>📎 A detailed PDF receipt is attached for your records.</p>"
            + "<div style='margin-top:40px;padding-top:24px;border-top:1px solid rgba(255,255,255,0.05);text-align:center'>"
            + "<p style='color:#64748B;font-size:12px'>Thank you for choosing RechargeHub.</p>"
            + "</div>"
            + "</div></div></body></html>";
    }

    // ── HTML body for FAILURE email ───────────────────────────────────────────
    private String buildFailureHtmlBody(PaymentEvent event) {
        return "<!DOCTYPE html><html><body style='font-family:\"Segoe UI\",Tahoma,sans-serif;background:#0F1113;color:#F1F5F9;padding:40px'>"
            + "<div style='max-width:600px;margin:auto;background:#181A1C;border:1px solid rgba(244,63,94,0.2);border-radius:24px;overflow:hidden;box-shadow:0 20px 40px rgba(0,0,0,0.4)'>"
            + "<div style='background:linear-gradient(135deg, #F43F5E, #9F1239);padding:40px;text-align:center'>"
            + "<h1 style='color:#FFFFFF;margin:0;font-size:28px;font-weight:800;letter-spacing:-1px'>Recharge Failed</h1>"
            + "</div>"
            + "<div style='padding:40px'>"
            + "<p style='font-size:18px;color:#F43F5E;font-weight:600'>Transaction Unsuccessful</p>"
            + "<p style='color:#94A3B8;line-height:1.6'>Unfortunately, your premium recharge could not be processed at this time. No amount has been deducted, or if it has, it will be refunded shortly.</p>"
            + "<div style='background:rgba(244,63,94,0.05);border-radius:16px;padding:24px;margin:32px 0;border:1px solid rgba(244,63,94,0.1)'>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + "<tr><td style='padding:8px 0;color:#94A3B8;font-size:13px'>Mobile Number</td><td style='padding:8px 0;text-align:right;font-weight:700;color:#F1F5F9'>" + nvl(event.getMobileNumber()) + "</td></tr>"
            + "<tr><td style='padding:8px 0;color:#94A3B8;font-size:13px'>Operator</td><td style='padding:8px 0;text-align:right;font-weight:700;color:#F1F5F9'>" + nvl(event.getOperatorName()) + "</td></tr>"
            + "<tr><td style='padding:16px 0 0;color:#94A3B8;font-size:13px'>Attempted Amount</td><td style='padding:16px 0 0;text-align:right;font-size:24px;font-weight:800;color:#F43F5E'>₹" + (event.getAmount() != null ? event.getAmount().toString() : "0.00") + "</td></tr>"
            + "</table>"
            + "</div>"
            + "<p style='text-align:center;color:#64748B;font-size:13px'>Please try again or contact our premium support team.</p>"
            + "<div style='margin-top:40px;padding-top:24px;border-top:1px solid rgba(255,255,255,0.05);text-align:center'>"
            + "<p style='color:#64748B;font-size:12px'>Thank you for choosing RechargeHub.</p>"
            + "</div>"
            + "</div></div></body></html>";
    }

    private String nvl(String val) {
        return val != null ? val : "N/A";
    }
}
