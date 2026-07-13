package com.capg.RechargeHub.dto;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : UserDto
 * DESCRIPTION:
 *   Data Transfer Object (DTO) used by the UserClient Feign interface
 *   to deserialize the response from user-service.
 *   Represents user profile data returned by GET /users/{id}.
 *   createdAt is kept as String to avoid Feign date parsing issues.
 * ================================================================ */
public class UserDto {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String phoneNumber;
    private String createdAt;

    /* ================================================================
     * CONSTRUCTOR: UserDto (no-args)
     * DESCRIPTION:
     *   Default constructor required for JSON deserialization by Feign.
     * ================================================================ */
    public UserDto() {}

    /* ================================================================
     * CONSTRUCTOR: UserDto (all-args)
     * DESCRIPTION:
     *   Parameterized constructor used in unit tests with Mockito
     *   to create mock user objects without a running user-service.
     * ================================================================ */
    public UserDto(Long id, String name, String email,
                   String role, String phoneNumber, String createdAt) {
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
    public String getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
