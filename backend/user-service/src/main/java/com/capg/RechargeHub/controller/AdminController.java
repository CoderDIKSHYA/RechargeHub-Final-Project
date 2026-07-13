package com.capg.RechargeHub.controller;

import com.capg.RechargeHub.dto.AuthResponse;
import com.capg.RechargeHub.dto.LoginRequest;
import com.capg.RechargeHub.dto.UserResponse;
import com.capg.RechargeHub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin APIs", description = "Administrative operations and login")
@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LogManager.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "Admin login and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new org.springframework.security.authentication.BadCredentialsException("Email and Password are required");
        }
        
        String email = request.getEmail().trim().toLowerCase();
        logger.info("Admin login attempt for: [{}]", email);
        
        request.setEmail(email); 
        AuthResponse response = userService.loginAdmin(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users [ADMIN only]")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/users/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Delete a user [ADMIN only]")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
