package com.capg.RechargeHub.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@rechargehub.com");
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
    }

    @Test
    void testSendEmail_Success() {
        emailService.sendEmail("to@e.com", "Sub", "Body");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail_Failure() {
        doThrow(new RuntimeException("Error")).when(mailSender).send(any(SimpleMailMessage.class));
        // Should catch and log, no exception thrown
        emailService.sendEmail("to@e.com", "Sub", "Body");
    }

    @Test
    void testSendHtmlEmail_Success() {
        emailService.sendHtmlEmail("to@e.com", "Sub", "<h1>Body</h1>");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendHtmlEmail_Failure() {
        doThrow(new RuntimeException("Error")).when(mailSender).send(any(MimeMessage.class));
        assertThrows(RuntimeException.class, () -> emailService.sendHtmlEmail("to@e.com", "Sub", "Body"));
    }

    @Test
    void testSendHtmlEmailWithAttachment_Success() {
        emailService.sendHtmlEmailWithAttachment("to@e.com", "Sub", "Body", new ByteArrayOutputStream(), "file.pdf");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendHtmlEmailWithAttachment_Failure() {
        doThrow(new RuntimeException("Error")).when(mailSender).send(any(MimeMessage.class));
        assertThrows(RuntimeException.class, () -> 
            emailService.sendHtmlEmailWithAttachment("to@e.com", "Sub", "Body", new ByteArrayOutputStream(), "f.pdf"));
    }

    @Test
    void testSendEmailWithAttachment_Success() {
        emailService.sendEmailWithAttachment("to@e.com", "Sub", "Body", new ByteArrayOutputStream(), "f.pdf");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailWithAttachment_Failure() {
        doThrow(new RuntimeException("Error")).when(mailSender).send(any(MimeMessage.class));
        assertThrows(RuntimeException.class, () -> 
            emailService.sendEmailWithAttachment("to@e.com", "Sub", "Body", new ByteArrayOutputStream(), "f.pdf"));
    }
}
