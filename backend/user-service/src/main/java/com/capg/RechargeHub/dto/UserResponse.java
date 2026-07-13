/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : UserResponse
 * DESCRIPTION:
 *   DTO representing the user profile data returned in API responses.
 *   Used by UserController for registration, login, and profile APIs.
 *   Excludes sensitive fields like password from the response.
 * ================================================================ */
package com.capg.RechargeHub.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String phoneNumber;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;


    public UserResponse() {}

    public UserResponse(Long id, String name, String email, String role, String phoneNumber, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}