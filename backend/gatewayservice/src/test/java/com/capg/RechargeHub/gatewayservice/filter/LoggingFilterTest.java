package com.capg.RechargeHub.gatewayservice.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private LoggingFilter loggingFilter;

    @Test
    void testLoggingFilter() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> result = loggingFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain, times(1)).filter(exchange);
        assertEquals(-1, loggingFilter.getOrder());
    }
}
