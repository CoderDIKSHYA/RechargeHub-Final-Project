/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : UserService
 * DESCRIPTION:
 *   Service layer for the User Service microservice.
 *   Handles user registration with password encoding,
 *   login with JWT token generation via Spring Security,
 *   and user profile retrieval by ID.
 *   Throws meaningful RuntimeExceptions for error scenarios.
 * ================================================================ */
package com.capg.RechargeHub.service;

import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.capg.RechargeHub.dto.AuthResponse;
import com.capg.RechargeHub.dto.LoginRequest;
import com.capg.RechargeHub.dto.UserRegistrationRequest;
import com.capg.RechargeHub.dto.UserResponse;
import com.capg.RechargeHub.entity.User;
import com.capg.RechargeHub.repository.UserRepository;
import com.capg.RechargeHub.security.JwtUtil;
import com.capg.RechargeHub.service.CloudinaryService;



@Slf4j
@Service
public class UserService {

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private com.capg.RechargeHub.repository.UserOtpRepository userOtpRepository;

    @Autowired
    private com.capg.RechargeHub.client.NotificationClient notificationClient;
    

    private String generateAndSendOtp(String email) {
        String lowerEmail = email.toLowerCase();
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        java.time.LocalDateTime expiry = java.time.LocalDateTime.now().plusMinutes(10);
        
        com.capg.RechargeHub.entity.UserOtp userOtp = userOtpRepository.findByEmail(lowerEmail).orElse(new com.capg.RechargeHub.entity.UserOtp());
        userOtp.setEmail(lowerEmail);
        userOtp.setOtp(otp);
        userOtp.setExpiryTime(expiry);
        userOtpRepository.save(userOtp);

        try {
            notificationClient.sendOtpEmail(new com.capg.RechargeHub.dto.OtpEmailRequest(lowerEmail, otp));
            logger.info("OTP sent to {}", lowerEmail);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}", lowerEmail, e);
        }
        return otp;
    }


    public UserResponse registerUser(UserRegistrationRequest request) {
        String lowerEmail = request.getEmail().toLowerCase();
        logger.info("Register request received for email: {}", lowerEmail);

        if (userRepository.findByEmail(lowerEmail).isPresent()) {
            logger.error("Registration failed - Email already exists: {}", lowerEmail);
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(lowerEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole("ROLE_USER");
        user.setVerified(false); // require OTP verification

        user = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", user.getId());

        generateAndSendOtp(user.getEmail());

        return mapToUserResponse(user);
    }

    public String verifyAccount(String email, String otp) {
        String lowerEmail = email.toLowerCase();
        com.capg.RechargeHub.entity.UserOtp userOtp = userOtpRepository.findByEmail(lowerEmail)
                .orElseThrow(() -> new RuntimeException("No OTP found for this email"));

        if (userOtp.isExpired()) {
            throw new RuntimeException("OTP has expired");
        }
        if (!userOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(lowerEmail).orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(true);
        userRepository.save(user);
        userOtpRepository.delete(userOtp);

        return "Account verified successfully";
    }

    public void resendOtp(String email) {
        String lowerEmail = email.toLowerCase();
        User user = userRepository.findByEmail(lowerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isVerified()) {
            throw new RuntimeException("Account already verified");
        }
        
        generateAndSendOtp(lowerEmail);
        logger.info("OTP resent to {}", lowerEmail);
    }

    public void forgotPassword(String email) {
        String lowerEmail = email.toLowerCase();
        User user = userRepository.findByEmail(lowerEmail)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));
        
        generateAndSendOtp(lowerEmail);
        logger.info("Forgot password OTP sent to {}", lowerEmail);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        String lowerEmail = email.toLowerCase();
        com.capg.RechargeHub.entity.UserOtp userOtp = userOtpRepository.findByEmail(lowerEmail)
                .orElseThrow(() -> new RuntimeException("No OTP found for this email"));

        if (userOtp.isExpired()) {
            throw new RuntimeException("OTP has expired");
        }
        if (!userOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(lowerEmail).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        userOtpRepository.delete(userOtp);
        
        logger.info("Password reset successfully for {}", lowerEmail);
    }

    public AuthResponse loginUser(LoginRequest request) {
        String lowerEmail = request.getEmail().toLowerCase();
        logger.info("User login attempt for email: {}", lowerEmail);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(lowerEmail, request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(lowerEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getRole().equals("ROLE_USER")) {
                throw new RuntimeException("Access denied. Use User Login for customer accounts.");
            }

            if (!user.isVerified()) {
                throw new RuntimeException("Account not verified. Please verify your email.");
            }

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
            logger.info("User logged in successfully: {}", user.getEmail());
            return new AuthResponse(token, mapToUserResponse(user));
        }
        throw new RuntimeException("Invalid credentials");
    }

    public AuthResponse loginAdmin(LoginRequest request) {
        if (request.getEmail() == null) {
            throw new BadCredentialsException("Email is required");
        }
        String lowerEmail = request.getEmail().trim().toLowerCase();
        logger.info("Admin login attempt for email: {}", lowerEmail);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(lowerEmail, request.getPassword())
            );

            if (authentication.isAuthenticated()) {
                User user = userRepository.findByEmail(lowerEmail)
                        .orElseThrow(() -> {
                            logger.error("Authenticated successfully but admin account NOT FOUND in DB: {}", lowerEmail);
                            return new BadCredentialsException("Admin account not found");
                        });

                if (!"ROLE_ADMIN".equals(user.getRole())) {
                    logger.warn("User {} logged in but has ROLE: {}. Access Denied for Admin Portal.", lowerEmail, user.getRole());
                    throw new BadCredentialsException("Access denied. Not an administrator.");
                }

                String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
                logger.info("Admin logged in successfully: {}", user.getEmail());
                return new AuthResponse(token, mapToUserResponse(user));
            }
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for admin email: {} - Reason: {}", lowerEmail, e.getMessage());
            throw e; 
        } catch (Exception e) {
            logger.error("Unexpected admin authentication error for {}: ", lowerEmail, e);
            throw new BadCredentialsException("Invalid administrator credentials");
        }
        logger.error("Authentication check reached end without success for {}", lowerEmail);
        throw new BadCredentialsException("Invalid credentials");
    }

    public AuthResponse verifyLogin(String email, String otp) {
        com.capg.RechargeHub.entity.UserOtp userOtp = userOtpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No OTP found for this email"));

        if (userOtp.isExpired()) {
            throw new RuntimeException("OTP has expired");
        }
        if (!userOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole(),
                user.getId()
        );

        userOtpRepository.delete(userOtp);
        logger.info("Login 2FA successful for user: {}", user.getEmail());

        return new AuthResponse(token, mapToUserResponse(user));
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {

        logger.info("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new RuntimeException("User not found");
                });

        logger.info("User fetched successfully: {}", user.getEmail());

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhoneNumber(),
                user.getCreatedAt()
        );
    }
    
    
    @CacheEvict(value = "users", key = "#userId")
    public String updateProfilePicture(Long userId, MultipartFile picture) throws IOException {

        logger.info("Updating profile picture for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", userId);
                    return new RuntimeException("User not found");
                });

        String url = cloudinaryService.uploadImage(picture);
        user.setProfilePictureUrl(url);
        userRepository.save(user);

        logger.info("Profile picture updated for user: {}", userId);

        return url;
    }

    // ── ADMIN operations ──────────────────────────────────────────────────────

    public UserResponse registerAdmin(UserRegistrationRequest request) {
        logger.info("Admin registration for email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.error("Admin registration failed - email exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole("ROLE_ADMIN");   // ← key difference

        user = userRepository.save(user);
        logger.info("Admin registered with ID: {}", user.getId());
        return mapToUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        logger.info("Admin: Fetching all users from database...");
        List<User> users = userRepository.findAll();
        logger.info("Admin: Found {} users in database", users.size());
        
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        logger.info("Deleting user id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        logger.info("User deleted id={}", id);
    }

}