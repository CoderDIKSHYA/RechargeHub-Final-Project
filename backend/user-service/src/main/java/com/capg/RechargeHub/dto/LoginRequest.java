/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : LoginRequest
 * DESCRIPTION:
 *   DTO for the user login request payload.
 *   Contains email and password with validation constraints.
 *   Used by UserController to authenticate users and issue JWT tokens.
 * ================================================================ */
package com.capg.RechargeHub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;


    public LoginRequest() {}

	public LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
		super();
		this.email = email;
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
    
}
