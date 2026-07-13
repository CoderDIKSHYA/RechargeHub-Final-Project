package com.capg.RechargeHub.gatewayservice.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private long expiration = 3600000;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", expiration);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken("user@example.com", "ROLE_USER", 101L);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token, "user@example.com"));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testExtractClaims() {
        String token = jwtUtil.generateToken("user@example.com", "ROLE_USER", 101L);
        assertEquals("user@example.com", jwtUtil.extractUsername(token));
        assertEquals("ROLE_USER", jwtUtil.extractRole(token));
        assertEquals(101L, jwtUtil.extractUserId(token));
        assertNotNull(jwtUtil.extractExpiration(token));
    }

    @Test
    void testValidateToken_InvalidUser() {
        String token = jwtUtil.generateToken("user@example.com", "ROLE_USER", 101L);
        assertFalse(jwtUtil.validateToken(token, "other@example.com"));
    }

    @Test
    void testValidateToken_InvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void testIsTokenExpired() {
        // Set very short expiration
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", -1000L);
        String token = jwtUtil.generateToken("user@example.com", "ROLE_USER", 101L);
        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void testIsTokenExpired_WithUser() {
        // Set very short expiration
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", -1000L);
        String token = jwtUtil.generateToken("user@example.com", "ROLE_USER", 101L);
        assertFalse(jwtUtil.validateToken(token, "user@example.com"));
    }
}
