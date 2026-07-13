package com.capg.RechargeHub.gatewayservice.filter;

import com.capg.RechargeHub.gatewayservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Test
    void testPublicPath() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/users/login")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void testOptionsMethod() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.OPTIONS, "/api/secure")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void testMissingAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/secure")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void testInvalidBearerPrefix() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, "Basic some-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void testInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void testValidToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.extractUserId("valid-token")).thenReturn(101L);
        when(jwtUtil.extractUsername("valid-token")).thenReturn("user@e.com");
        when(jwtUtil.extractRole("valid-token")).thenReturn("ROLE_USER");
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testTokenValidationException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer error-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(anyString())).thenThrow(new RuntimeException("Parsing error"));

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }
}
