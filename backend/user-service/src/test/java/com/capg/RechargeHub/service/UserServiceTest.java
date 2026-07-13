/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : UserServiceTest
 * DESCRIPTION:
 *   Unit test class for UserService.
 *   Uses Mockito to mock UserRepository, PasswordEncoder,
 *   JwtUtil, AuthenticationManager, and CloudinaryService.
 *   Tests cover all branches: registration success/duplicate,
 *   login success/not-authenticated, getUserById found/not-found,
 *   and updateProfilePicture success/not-found.
 * ================================================================ */
package com.capg.RechargeHub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.capg.RechargeHub.dto.*;
import com.capg.RechargeHub.entity.User;
import com.capg.RechargeHub.entity.UserOtp;
import com.capg.RechargeHub.repository.UserRepository;
import com.capg.RechargeHub.repository.UserOtpRepository;
import com.capg.RechargeHub.client.NotificationClient;
import com.capg.RechargeHub.security.JwtUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private UserOtpRepository userOtpRepository;

    @Mock
    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setPhoneNumber("1234567890");
        user.setRole("ROLE_USER");
        user.setVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Test
    void testRegisterUserSuccess() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setPhoneNumber("1234567890");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userOtpRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        UserResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
    }

    @Test
    void testRegisterUserDuplicateEmail() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("john@example.com");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(buildUser()));

        assertThrows(RuntimeException.class, () -> userService.registerUser(request));
    }

    @Test
    void testLoginUserSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        User user = buildUser();
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(userOtpRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        AuthResponse response = userService.loginUser(request);

        assertNotNull(response);
        assertEquals("OTP_SENT", response.getToken());
    }

    @Test
    void testLoginUserNotAuthenticated() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        assertThrows(RuntimeException.class, () -> userService.loginUser(request));
    }

    @Test
    void testGetUserByIdFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser()));
        UserResponse response = userService.getUserById(1L);
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.getUserById(99L));
    }

    @Test
    void testUpdateProfilePictureSuccess() throws IOException {
        User user = buildUser();
        MockMultipartFile file = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "fake".getBytes());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cloudinaryService.uploadImage(file)).thenReturn("http://url.com");

        String url = userService.updateProfilePicture(1L, file);
        assertEquals("http://url.com", url);
    }

    @Test
    void testRegisterAdminSuccess() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Admin User");
        request.setEmail("admin@example.com");
        User adminUser = buildUser();
        adminUser.setRole("ROLE_ADMIN");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("enc");
        when(userRepository.save(any())).thenReturn(adminUser);

        UserResponse response = userService.registerAdmin(request);
        assertEquals("ROLE_ADMIN", response.getRole());
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(buildUser()));
        assertEquals(1, userService.getAllUsers().size());
    }

    @Test
    void testDeleteUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser()));
        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).delete(any());
    }

    @Test
    void testVerifyAccount_Success() {
        String email = "test@example.com";
        UserOtp userOtp = new UserOtp();
        userOtp.setOtp("123456");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        User user = buildUser();

        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.of(userOtp));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertEquals("Account verified successfully", userService.verifyAccount(email, "123456"));
    }

    @Test
    void testVerifyAccount_InvalidOtp() {
        String email = "test@example.com";
        UserOtp userOtp = new UserOtp();
        userOtp.setOtp("111111");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.of(userOtp));

        assertThrows(RuntimeException.class, () -> userService.verifyAccount(email, "222222"));
    }

    @Test
    void testVerifyLogin_Success() {
        String email = "test@example.com";
        UserOtp userOtp = new UserOtp();
        userOtp.setOtp("123456");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        User user = buildUser();

        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.of(userOtp));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("mock-token");

        AuthResponse response = userService.verifyLogin(email, "123456");
        assertEquals("mock-token", response.getToken());
    }

    @Test
    void testGenerateAndSendOtp_ExistingOtp() {
        String email = "test@example.com";
        UserOtp existing = new UserOtp();
        existing.setEmail(email);
        existing.setOtp("000000");
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.of(existing));
        
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("enc");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.registerUser(request);
        assertNotEquals("000000", existing.getOtp());
    }

    @Test
    void testGenerateAndSendOtp_NotificationFailure() {
        String email = "test@example.com";
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Error")).when(notificationClient).sendOtpEmail(any());
        User user = buildUser();
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("enc");
        when(userRepository.save(any())).thenReturn(user);

        assertDoesNotThrow(() -> userService.registerUser(new UserRegistrationRequest()));
    }

    @Test
    void testVerifyAccount_ExpiredOtp() {
        UserOtp userOtp = new UserOtp();
        userOtp.setExpiryTime(LocalDateTime.now().minusMinutes(1));
        when(userOtpRepository.findByEmail(any())).thenReturn(Optional.of(userOtp));
        assertThrows(RuntimeException.class, () -> userService.verifyAccount("a@b.com", "1"));
    }

    @Test
    void testLoginUser_AdminBypassVerification() {
        User admin = buildUser();
        admin.setRole("ROLE_ADMIN");
        admin.setVerified(false);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(admin));

        AuthResponse response = userService.loginUser(new LoginRequest());
        assertEquals("OTP_SENT", response.getToken());
    }

    @Test
    void testLoginUser_UnverifiedUserFails() {
        User user = buildUser();
        user.setVerified(false);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.loginUser(new LoginRequest()));
    }

    @Test
    void testVerifyLogin_ExpiredOtp() {
        UserOtp userOtp = new UserOtp();
        userOtp.setExpiryTime(LocalDateTime.now().minusMinutes(1));
        when(userOtpRepository.findByEmail(any())).thenReturn(Optional.of(userOtp));
        assertThrows(RuntimeException.class, () -> userService.verifyLogin("a@b.com", "1"));
    }

    @Test
    void testRegisterAdmin_DuplicateEmail() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("admin@exists.com");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));
        assertThrows(RuntimeException.class, () -> userService.registerAdmin(request));
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.deleteUser(99L));
    }

    @Test
    void testUpdateProfilePicture_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.updateProfilePicture(99L, null));
    }

    @Test
    void testUpdateProfilePicture_CloudinaryFailure() throws IOException {
        User user = buildUser();
        MockMultipartFile file = new MockMultipartFile("pic", "test.jpg", "image/jpeg", "data".getBytes());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cloudinaryService.uploadImage(any())).thenThrow(new IOException("Cloudinary Down"));
        
        assertThrows(IOException.class, () -> userService.updateProfilePicture(1L, file));
    }

    @Test
    void testVerifyAccount_UserNotFound() {
        UserOtp userOtp = new UserOtp();
        userOtp.setOtp("123456");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        when(userOtpRepository.findByEmail(anyString())).thenReturn(Optional.of(userOtp));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.verifyAccount("a@b.com", "123456"));
    }

    @Test
    void testVerifyLogin_UserNotFound() {
        UserOtp userOtp = new UserOtp();
        userOtp.setOtp("123456");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        when(userOtpRepository.findByEmail(anyString())).thenReturn(Optional.of(userOtp));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.verifyLogin("a@b.com", "123456"));
    }

    @Test
    void testLoginUser_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("none@e.com");
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.loginUser(request));
    }

    @Test
    void testVerifyAccount_NoOtpFound() {
        when(userOtpRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.verifyAccount("a@b.com", "123456"));
    }

    @Test
    void testVerifyLogin_NoOtpFound() {
        when(userOtpRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.verifyLogin("a@b.com", "123456"));
    }

    @Test
    void testVerifyLogin_InvalidOtp() {
        UserOtp userOtp = new UserOtp();
        userOtp.setOtp("111111");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        when(userOtpRepository.findByEmail(anyString())).thenReturn(Optional.of(userOtp));
        assertThrows(RuntimeException.class, () -> userService.verifyLogin("a@b.com", "222222"));
    }
}
