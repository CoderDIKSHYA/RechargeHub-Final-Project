/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : UserRegistrationRequest
 * DESCRIPTION:
 *   DTO for the user registration request payload.
 *   Validations:
 *     - name: only letters and spaces, no numbers/special chars
 *     - email: valid email format
 *     - password: minimum 6 characters
 *     - phoneNumber: exactly 10 digits, starts with 6-9 (Indian mobile)
 * ================================================================ */
package com.capg.RechargeHub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegistrationRequest {

    @NotBlank(message = "Name is required")
    @Pattern(
        regexp = "^[a-zA-Z ]+$",
        message = "Name must contain only letters and spaces — no numbers or special characters"
    )
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^[6-9]\\d{9}$",
        message = "Phone number must be exactly 10 digits and start with 6, 7, 8, or 9"
    )
    private String phoneNumber;

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
