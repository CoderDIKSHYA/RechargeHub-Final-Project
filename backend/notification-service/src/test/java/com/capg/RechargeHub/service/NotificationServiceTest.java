/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : NotificationServiceTest
 * DESCRIPTION:
 *   Unit test class for NotificationService.
 *   Uses Mockito to mock NotificationRepository.
 *   Tests cover getAllNotifications, getNotificationById (found and
 *   not-found), and getNotificationsByUserId scenarios.
 *   No Oracle DB, RabbitMQ, or Docker required to run these tests.
 * ================================================================ */
package com.capg.RechargeHub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capg.RechargeHub.dto.NotificationResponse;
import com.capg.RechargeHub.entity.Notification;
import com.capg.RechargeHub.repository.NotificationRepository;
import com.capg.RechargeHub.service.NotificationService;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * Unit tests for NotificationService.
 * These tests validate service behavior and provide regression coverage.
 * Comments and logging are used in service layer for tracing.
 */
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setup() {
        notification = new Notification(
                1L,
                101L,
                "Test Message",
                "SMS",
                "SENT",
                LocalDateTime.now()
        );
    }

    // ✅ Test 1: Get All Notifications
    @Test
    void testGetAllNotifications() {
        when(notificationRepository.findAll()).thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.getAllNotifications();

        assertEquals(1, result.size());
        assertEquals("Test Message", result.get(0).getMessage());
    }

    // ✅ Test 2: Get Notification By ID (SUCCESS)
    @Test
    void testGetNotificationById_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.getNotificationById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    // ❌ Test 3: Get Notification By ID (FAILURE)
    @Test
    void testGetNotificationById_NotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            notificationService.getNotificationById(1L);
        });
    }

    @Test
    void testGetNotificationById_GeneralException() {
        when(notificationRepository.findById(anyLong())).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> {
            notificationService.getNotificationById(1L);
        });
    }

    // ✅ Test 4: Get Notifications By User ID
    @Test
    void testGetNotificationsByUserId() {
        when(notificationRepository.findByUserId(101L)).thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.getNotificationsByUserId(101L);

        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getUserId());
    }

    @Test
    void testGetAllNotifications_Exception() {
        when(notificationRepository.findAll()).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> notificationService.getAllNotifications());
    }

    @Test
    void testGetNotificationsByUserId_Exception() {
        when(notificationRepository.findByUserId(anyLong())).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> notificationService.getNotificationsByUserId(1L));
    }
}