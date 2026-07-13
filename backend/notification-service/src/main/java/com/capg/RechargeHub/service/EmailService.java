/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : EmailService
 * DESCRIPTION:
 *   Email sending service for the Notification Service.
 *
 *   Methods:
 *   1. sendEmail()                  — plain text email (legacy/fallback)
 *   2. sendHtmlEmail()              — HTML email (failure notifications)
 *   3. sendHtmlEmailWithAttachment()— HTML email + PDF receipt (success)
 *   4. sendEmailWithAttachment()    — plain text + attachment (legacy)
 *
 *   All methods catch exceptions internally and log errors
 *   so a mail failure never crashes the payment flow.
 * ================================================================ */
package com.capg.RechargeHub.service;

import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── 1. Plain text email ───────────────────────────────────────────────────
    public void sendEmail(String to, String subject, String body) {
        try {
            logger.info("Sending plain text email to: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("Plain text email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send plain text email to {}: {}", to, e.getMessage());
        }
    }

    // ── 2. HTML email (no attachment) ─────────────────────────────────────────
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            logger.info("Sending HTML email to: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            mailSender.send(message);
            logger.info("HTML email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email delivery failed", e);
        }
    }

    // ── 3. HTML email + PDF attachment (SUCCESS receipt) ─────────────────────
    public void sendHtmlEmailWithAttachment(String to, String subject, String htmlBody,
                                            ByteArrayOutputStream attachment, String fileName) {
        try {
            logger.info("Sending HTML email with PDF attachment to: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            helper.addAttachment(fileName, new ByteArrayResource(attachment.toByteArray()));
            mailSender.send(message);
            logger.info("HTML email with PDF attachment '{}' sent to: {}", fileName, to);
        } catch (Exception e) {
            logger.error("Failed to send HTML email with attachment to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email delivery with attachment failed", e);
        }
    }

    // ── 4. Plain text + attachment (kept for backward compatibility) ──────────
    public void sendEmailWithAttachment(String to, String subject, String body,
                                        ByteArrayOutputStream attachment, String fileName) {
        try {
            logger.info("Sending plain text email with attachment to: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(fileName, new ByteArrayResource(attachment.toByteArray()));
            mailSender.send(message);
            logger.info("Plain text email with attachment sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send plain text email with attachment to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email delivery (legacy) failed", e);
        }
    }
}
