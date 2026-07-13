package com.capg.RechargeHub.gatewayservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.capg.RechargeHub.gatewayservice.util.JwtUtil;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
    }

    // ✅ PUBLIC ENDPOINTS (CENTRALIZED)
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/users/register",
            "/users/login",
            "/admin/login",
            "/users/forgot-password",
            "/users/reset-password",
            "/login",
            "/register",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator/health"
    );

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            // Log incoming path for debugging
            logger.debug("Incoming Path: {}", path);

            // ✅ FIXED: Proper matching
            if (isPublicPath(path) || exchange.getRequest().getMethod().name().equals("OPTIONS")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            // ❌ Missing token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // ❌ Invalid token
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // ✅ Extract user data
                Long userId = jwtUtil.extractUserId(token);
                String email = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                // ✅ Add headers
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "Token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * ✅ FIXED: Strong path matching using startsWith
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    /**
     * ✅ Improved error handling
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"error\": \"%s\", \"status\": %d}",
                message,
                status.value()
        );

        return exchange.getResponse().writeWith(
                Flux.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }
}