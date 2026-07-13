/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : SecurityConfig (operator-service)
 * DESCRIPTION:
 *   Security configuration for the Operator Service.
 *
 *   Role enforcement (via X-User-Role header forwarded by Gateway):
 *     - GET  /operators/**  → any authenticated user (USER or ADMIN)
 *     - GET  /plans/**      → any authenticated user
 *     - POST/PUT/DELETE     → ROLE_ADMIN only
 *
 *   The Gateway already validates the JWT and injects X-User-Role.
 *   This filter reads that header and sets the SecurityContext.
 * ================================================================ */
package com.capg.RechargeHub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Filter that reads X-User-Role and X-User-Email headers
     * injected by the API Gateway after JWT validation,
     * and sets the Spring Security context accordingly.
     */
    @Bean
    public OncePerRequestFilter gatewayHeaderAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {

                String role  = request.getHeader("X-User-Role");
                String email = request.getHeader("X-User-Email");

                if (role != null && email != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    email, null,
                                    List.of(new SimpleGrantedAuthority(role)));

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger & actuator — open
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/**").permitAll()

                        // READ operations — any authenticated user (USER or ADMIN)
                        .requestMatchers(HttpMethod.GET, "/operators/**").authenticated()

                        // WRITE operations — ADMIN only
                        .requestMatchers(HttpMethod.POST,   "/operators/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/operators/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/operators/**").hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayHeaderAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
