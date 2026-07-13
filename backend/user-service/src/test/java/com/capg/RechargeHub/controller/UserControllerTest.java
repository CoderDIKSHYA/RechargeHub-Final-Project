/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : UserControllerTest
 * DESCRIPTION:
 *   Unit test class for UserController.
 *   Uses MockMvc standalone setup to test HTTP layer without
 *   loading the full Spring context or requiring Oracle/Eureka.
 *   Mocks UserService to isolate controller behavior.
 *   Tests cover POST /users/register, POST /users/login,
 *   and GET /users/{id} endpoints.
 * ================================================================ */
package com.capg.RechargeHub.controller;

import com.capg.RechargeHub.dto.AuthResponse;
import com.capg.RechargeHub.dto.LoginRequest;
import com.capg.RechargeHub.dto.UserRegistrationRequest;
import com.capg.RechargeHub.dto.UserResponse;
import com.capg.RechargeHub.dto.OtpEmailRequest;
import com.capg.RechargeHub.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new com.capg.RechargeHub.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private UserResponse buildUserResponse() {
        return new UserResponse(1L, "John Doe", "john@example.com", "ROLE_USER", "9876543210", LocalDateTime.now());
    }

    // ✅ TEST: Register User - 201 Created
    @Test
    void testRegisterUser_Returns201() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setPhoneNumber("9876543210");

        when(userService.registerUser(any())).thenReturn(buildUserResponse());

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    // ✅ TEST: Login User - 200 OK
    @Test
    void testLoginUser_Returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        AuthResponse authResponse = new AuthResponse("OTP_SENT", buildUserResponse());

        when(userService.loginUser(any())).thenReturn(authResponse);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("OTP_SENT"));
    }

    // ✅ TEST: Get User By ID - 200 OK
    @Test
    void testGetUserById_Returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(buildUserResponse());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testUploadProfilePicture_Returns200() throws Exception {
        MockMultipartFile picture = new MockMultipartFile(
                "picture", "test.jpg", "image/jpeg", "fake-image".getBytes());

        when(userService.updateProfilePicture(eq(1L), any()))
                .thenReturn("http://cloudinary.com/test.jpg");

        mockMvc.perform(multipart("/users/profile/picture")
                        .file(picture)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl").value("http://cloudinary.com/test.jpg"));
    }

    // ✅ TEST: Register Admin - 201 Created
    @Test
    void testRegisterAdmin_Returns201() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Admin");
        request.setEmail("admin@example.com");
        request.setPassword("admin123");
        request.setPhoneNumber("9999999999");

        UserResponse adminResponse = new UserResponse(
                2L, "Admin", "admin@example.com", "ROLE_ADMIN", "9999999999", LocalDateTime.now());

        when(userService.registerAdmin(any())).thenReturn(adminResponse);

        mockMvc.perform(post("/users/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    // ✅ TEST: Get All Users - 200 OK
    @Test
    void testGetAllUsers_Returns200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(buildUserResponse()));

        mockMvc.perform(get("/users/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ✅ TEST: Delete User - 200 OK
    @Test
    void testDeleteUser_Returns200() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void testVerifyAccount_Returns200() throws Exception {
        OtpEmailRequest request = new OtpEmailRequest("test@example.com", "123456");
        when(userService.verifyAccount(any(), any())).thenReturn("Success");

        mockMvc.perform(post("/users/verify-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void testVerifyLogin_Returns200() throws Exception {
        OtpEmailRequest request = new OtpEmailRequest("test@example.com", "123456");
        when(userService.verifyLogin(any(), any())).thenReturn(new AuthResponse("token", buildUserResponse()));

        mockMvc.perform(post("/users/verify-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }
}
