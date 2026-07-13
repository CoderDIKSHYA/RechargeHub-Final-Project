/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : Notification
 * DESCRIPTION:
 *   JPA entity representing a notification record.
 *   Maps to the "notifications" table in the MySQL database.
 * ================================================================ */
package com.capg.RechargeHub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(Long id, Long userId, String message, String type, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
