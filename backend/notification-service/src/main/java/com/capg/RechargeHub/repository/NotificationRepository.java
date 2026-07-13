/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : NotificationRepository
 * DESCRIPTION:
 *   Spring Data JPA repository for the Notification entity.
 *   Provides standard CRUD operations and a custom query method
 *   to fetch all notifications for a specific user ID.
 * ================================================================ */
package com.capg.RechargeHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capg.RechargeHub.entity.Notification;

import java.util.List;

/*
 * Repository layer for Notification entities.
 * It abstracts DB access for notifications and is used by NotificationService.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
}
