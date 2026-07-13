/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : UserController
 * DESCRIPTION:
 *   REST controller for the User Service.
 *
 *   PUBLIC endpoints (no token needed):
 *     POST /users/register       — register as ROLE_USER
 *     POST /users/login          — get JWT token
 *
 *   USER endpoints (any valid JWT):
 *     GET  /users/{id}           — get own profile
 *     PUT  /users/profile/picture— upload profile picture
 *
 *   ADMIN-only endpoints (ROLE_ADMIN JWT required):
 *     POST /users/admin/register — create admin account
 *     GET  /users/admin/all      — list all users
 *     DELETE /users/admin/{id}   — delete a user
 * ================================================================ */
package com.capg.RechargeHub.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.capg.RechargeHub.dto.AuthResponse;
import com.capg.RechargeHub.dto.LoginRequest;
import com.capg.RechargeHub.dto.UserRegistrationRequest;
import com.capg.RechargeHub.dto.UserResponse;
import com.capg.RechargeHub.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "User APIs", description = "User registration, login, and profile management")
@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // ── PUBLIC ────────────────────────────────────────────────────────────────

    @Operation(summary = "Register a new user (ROLE_USER)")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        logger.info("Registration request for email: {}", request.getEmail());
        UserResponse response = userService.registerUser(request);
        logger.info("User registered: {}", response.getEmail());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Login and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {
        logger.info("Login request for email: {}", request.getEmail());
        AuthResponse response = userService.loginUser(request);
        logger.info("Login successful for: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify account registration OTP")
    @PostMapping("/verify-account")
    public ResponseEntity<Map<String, String>> verifyAccount(
            @RequestBody com.capg.RechargeHub.dto.OtpEmailRequest request) {
        logger.info("Verify account request for email: {}", request.getEmail());
        String msg = userService.verifyAccount(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", msg));
    }

    @Operation(summary = "Verify login OTP and get JWT")
    @PostMapping("/verify-login")
    public ResponseEntity<AuthResponse> verifyLogin(
            @RequestBody com.capg.RechargeHub.dto.OtpEmailRequest request) {
        logger.info("Verify login request for email: {}", request.getEmail());
        AuthResponse response = userService.verifyLogin(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Resend OTP for account verification")
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Resend OTP request for email: {}", email);
        userService.resendOtp(email);
        return ResponseEntity.ok(Map.of("message", "OTP resent successfully"));
    }

    @Operation(summary = "Forgot password - send OTP")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Forgot password request for email: {}", email);
        userService.forgotPassword(email);
        return ResponseEntity.ok(Map.of("message", "Forgot password OTP sent"));
    }

    @Operation(summary = "Reset password with OTP")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody com.capg.RechargeHub.dto.ResetPasswordRequest request) {
        logger.info("Reset password request for email: {}", request.getEmail());
        userService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // ── USER (authenticated) ──────────────────────────────────────────────────

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.info("Fetching user id={}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Upload profile picture")
    @PutMapping("/profile/picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @RequestPart("picture") MultipartFile picture,
            @RequestHeader("X-User-Id") Long userId) throws IOException {
        logger.info("Profile picture upload for userId={}", userId);
        String url = userService.updateProfilePicture(userId, picture);
        return new ResponseEntity<>(Map.of("profilePictureUrl", url), HttpStatus.OK);
    }

    // ── ADMIN-ONLY ────────────────────────────────────────────────────────────

    @Operation(summary = "Register a new ADMIN user [ADMIN only]")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/admin/register")
    public ResponseEntity<UserResponse> registerAdmin(
            @Valid @RequestBody UserRegistrationRequest request) {
        logger.info("Admin registration request for email: {}", request.getEmail());
        UserResponse response = userService.registerAdmin(request);
        logger.info("Admin registered: {}", response.getEmail());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all users [ADMIN only]")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Admin: fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Delete a user by ID [ADMIN only]")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        logger.info("Admin: deleting userId={}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}