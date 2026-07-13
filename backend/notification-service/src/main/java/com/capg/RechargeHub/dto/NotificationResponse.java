/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : NotificationResponse
 * DESCRIPTION:
 *   DTO representing notification data returned in API responses.
 *   Used by NotificationController to serialize notification records
 *   as JSON for the client.
 * ================================================================ */
package com.capg.RechargeHub.dto;

import java.time.LocalDateTime;

/*
 * Data Transfer Object (DTO) used by NotificationController to return
 * notification data as JSON response.
 */
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String message;
    private String type;
    private String status;
    private LocalDateTime createdAt;

    public NotificationResponse() {}

    public NotificationResponse(Long id, Long userId, String message, String type, String status, LocalDateTime createdAt) {
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
