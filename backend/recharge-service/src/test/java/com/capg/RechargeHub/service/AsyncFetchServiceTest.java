/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : AsyncFetchServiceTest
 * DESCRIPTION:
 *   Unit test class for AsyncFetchService.
 *   Uses Mockito to mock UserClient and OperatorClient.
 *   Tests cover fetchUserAsync, fetchOperatorAsync, and fetchPlanAsync
 *   to ensure CompletableFuture results are returned correctly.
 *   The @Async annotation is bypassed in unit tests (no thread pool needed).
 * ================================================================ */
package com.capg.RechargeHub.service;

import com.capg.RechargeHub.client.OperatorClient;
import com.capg.RechargeHub.client.UserClient;
import com.capg.RechargeHub.dto.OperatorDto;
import com.capg.RechargeHub.dto.PlanDto;
import com.capg.RechargeHub.dto.UserDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncFetchServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private OperatorClient operatorClient;

    @InjectMocks
    private AsyncFetchService asyncFetchService;

    // ✅ TEST 1: fetchUserAsync returns user DTO
    @Test
    void testFetchUserAsync() throws ExecutionException, InterruptedException {
        UserDto userDto = new UserDto();
        when(userClient.getUserById(1L)).thenReturn(userDto);

        CompletableFuture<UserDto> future = asyncFetchService.fetchUserAsync(1L);

        assertNotNull(future);
        assertEquals(userDto, future.get());
        verify(userClient, times(1)).getUserById(1L);
    }

    // ✅ TEST 2: fetchOperatorAsync returns operator DTO
    @Test
    void testFetchOperatorAsync() throws ExecutionException, InterruptedException {
        OperatorDto operatorDto = new OperatorDto(1L, "Jio");
        when(operatorClient.getOperatorById(1L)).thenReturn(operatorDto);

        CompletableFuture<OperatorDto> future = asyncFetchService.fetchOperatorAsync(1L);

        assertNotNull(future);
        assertEquals(operatorDto, future.get());
        verify(operatorClient, times(1)).getOperatorById(1L);
    }

    // ✅ TEST 3: fetchPlanAsync returns plan DTO
    @Test
    void testFetchPlanAsync() throws ExecutionException, InterruptedException {
        PlanDto planDto = new PlanDto(1L, 1L, BigDecimal.valueOf(299), "28 days", "Unlimited");
        when(operatorClient.getPlanById(1L)).thenReturn(planDto);

        CompletableFuture<PlanDto> future = asyncFetchService.fetchPlanAsync(1L);

        assertNotNull(future);
        assertEquals(planDto, future.get());
        verify(operatorClient, times(1)).getPlanById(1L);
    }
}
