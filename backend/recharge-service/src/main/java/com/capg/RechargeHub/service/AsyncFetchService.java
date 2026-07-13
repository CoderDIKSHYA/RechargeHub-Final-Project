package com.capg.RechargeHub.service;

import com.capg.RechargeHub.client.OperatorClient;
import com.capg.RechargeHub.client.UserClient;
import com.capg.RechargeHub.dto.OperatorDto;
import com.capg.RechargeHub.dto.PlanDto;
import com.capg.RechargeHub.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Async service that fetches user, operator, and plan data in parallel
 * using @Async + CompletableFuture to satisfy the parallel/asynchronous design requirement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncFetchService {

    private final UserClient userClient;
    private final OperatorClient operatorClient;

    @Async("rechargeTaskExecutor")
    public CompletableFuture<UserDto> fetchUserAsync(Long userId) {
        log.info("Async fetch: user {}", userId);
        return CompletableFuture.completedFuture(userClient.getUserById(userId));
    }

    @Async("rechargeTaskExecutor")
    public CompletableFuture<OperatorDto> fetchOperatorAsync(Long operatorId) {
        log.info("Async fetch: operator {}", operatorId);
        return CompletableFuture.completedFuture(operatorClient.getOperatorById(operatorId));
    }

    @Async("rechargeTaskExecutor")
    public CompletableFuture<PlanDto> fetchPlanAsync(Long planId) {
        log.info("Async fetch: plan {}", planId);
        return CompletableFuture.completedFuture(operatorClient.getPlanById(planId));
    }
}
