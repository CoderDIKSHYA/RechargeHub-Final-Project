/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : NotificationService
 * DESCRIPTION:
 *   Service layer for the Notification Service microservice.
 *   Provides methods to retrieve notifications from the database
 *   by ID, user ID, or all records.
 *   Maps Notification entities to NotificationResponse DTOs.
 * ================================================================ */
package com.capg.RechargeHub.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capg.RechargeHub.dto.NotificationResponse;
import com.capg.RechargeHub.entity.Notification;
import com.capg.RechargeHub.repository.NotificationRepository;

import java.util.List;
import java.util.stream.Collectors;

/*
 * This class handles business logic for notifications.
 * It acts as the service layer in the microservice architecture.
 * Logging is added for method entry, success, and exceptions.
 */
@Service
public class NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        logger.info("Entering getNotificationsByUserId userId={}", userId);
        try {
            List<NotificationResponse> result = notificationRepository.findByUserId(userId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            logger.info("Successfully fetched {} notifications for user {}", result.size(), userId);
            return result;
        } catch (Exception e) {
            logger.error("Error in getNotificationsByUserId userId={}", userId, e);
            throw e;
        }
    }

    public NotificationResponse getNotificationById(Long id) {
        logger.info("Entering getNotificationById id={}", id);
        try {
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));
            NotificationResponse response = mapToResponse(notification);
            logger.info("Successfully fetched notification id={}", id);
            return response;
        } catch (Exception e) {
            logger.error("Error in getNotificationById id={}", id, e);
            throw e;
        }
    }
    
    public List<NotificationResponse> getAllNotifications() {
        logger.info("Entering getAllNotifications");
        try {
            List<NotificationResponse> result = notificationRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            logger.info("Successfully fetched all notifications count={}", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error in getAllNotifications", e);
            throw e;
        }
    }

    public NotificationResponse broadcastNotification(String message, String type) {
        logger.info("Broadcasting notification: {}", message);
        Notification notification = new Notification();
        notification.setUserId(0L); // 0L represents a Global/Broadcast notification
        notification.setMessage(message);
        notification.setType(type);
        notification.setStatus("UNREAD");
        notification.setCreatedAt(java.time.LocalDateTime.now());
        
        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setStatus(notification.getStatus());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
