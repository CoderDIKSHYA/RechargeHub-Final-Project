package com.capg.RechargeHub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.capg.RechargeHub.client.*;
import com.capg.RechargeHub.dto.*;
import com.capg.RechargeHub.entity.Recharge;
import com.capg.RechargeHub.repository.RechargeRepository;
import com.capg.RechargeHub.service.RechargeService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RechargeServiceTest {

    @InjectMocks
    private RechargeService rechargeService;

    @Mock
    private RechargeRepository rechargeRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private OperatorClient operatorClient;

    @Mock
    private PaymentClient paymentClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Recharge buildRecharge(Long id, String status) {
        Recharge r = new Recharge();
        r.setId(id);
        r.setUserId(501L);
        r.setOperatorId(101L);
        r.setPlanId(201L);
        r.setMobileNumber("9999999999");
        r.setAmount(BigDecimal.valueOf(299));
        r.setStatus(status);
        // simulate @PrePersist so mapToResponse never NPEs on createdAt
        try {
            var m = Recharge.class.getDeclaredMethod("onCreate");
            m.setAccessible(true);
            m.invoke(r);
        } catch (Exception ignored) {}
        return r;
    }

    private RechargeRequest buildRequest() {
        RechargeRequest req = new RechargeRequest();
        req.setOperatorId(101L);
        req.setPlanId(201L);
        req.setMobileNumber("9999999999");
        req.setPaymentMethod("UPI");
        return req;
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void testInitiateRecharge_Success() {
        Long userId = 501L;

        when(userClient.getUserById(userId)).thenReturn(new UserDto());

        OperatorDto operator = new OperatorDto(101L, "Airtel");
        when(operatorClient.getOperatorById(101L)).thenReturn(operator);

        PlanDto plan = new PlanDto(201L, 101L, BigDecimal.valueOf(299), "28 days", "Unlimited");
        when(operatorClient.getPlanById(201L)).thenReturn(plan);

        Recharge pending  = buildRecharge(1L, "PENDING");
        when(rechargeRepository.save(any())).thenReturn(pending);

        RechargeResponse response = rechargeService.initiateRecharge(userId, buildRequest());

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals(userId, response.getUserId());
        assertEquals(101L, response.getOperatorId());
        assertEquals(201L, response.getPlanId());
        assertEquals("PENDING", response.getMessage());

        verify(userClient).getUserById(userId);
        verify(operatorClient).getOperatorById(101L);
        verify(operatorClient).getPlanById(201L);
        verify(rechargeRepository, times(2)).save(any());
    }

    @Test
    void testUpdateRechargeStatus_Success() {
        Recharge recharge = buildRecharge(1L, "PENDING");
        when(rechargeRepository.findById(1L)).thenReturn(Optional.of(recharge));
        when(rechargeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RechargeResponse response = rechargeService.updateRechargeStatus(1L, "SUCCESS", "txn_123");

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("txn_123", response.getTransactionId());
        verify(rechargeRepository).save(any());
    }

    @Test
    void testUpdateRechargeStatus_NullTransactionId_And_NonSuccess() {
        Recharge recharge = buildRecharge(1L, "PENDING");
        when(rechargeRepository.findById(1L)).thenReturn(Optional.of(recharge));
        when(rechargeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Test with null transactionId and FAILED status (to skip Success log branch)
        RechargeResponse response = rechargeService.updateRechargeStatus(1L, "FAILED", null);

        assertEquals("FAILED", response.getStatus());
        assertNull(response.getTransactionId());
        verify(rechargeRepository).save(any());
    }

    @Test
    void testUpdateRechargeStatus_NotFound() {
        when(rechargeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            rechargeService.updateRechargeStatus(99L, "SUCCESS", "txn_123"));
    }

    @Test
    void testInitiateRecharge_UserNotFound() {

        Long userId = 501L;
        when(userClient.getUserById(userId)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rechargeService.initiateRecharge(userId, buildRequest()));

        assertEquals("User not found", ex.getMessage());
        verify(userClient).getUserById(userId);
        verifyNoInteractions(operatorClient, paymentClient, rechargeRepository);
    }

    @Test
    void testInitiateRecharge_InvalidPlan_OperatorMismatch() {

        Long userId = 501L;
        when(userClient.getUserById(userId)).thenReturn(new UserDto());

        OperatorDto operator = new OperatorDto(101L, "Airtel");
        when(operatorClient.getOperatorById(101L)).thenReturn(operator);

        // plan belongs to a different operator
        PlanDto plan = new PlanDto(201L, 999L, BigDecimal.valueOf(299), "28 days", "Unlimited");
        when(operatorClient.getPlanById(201L)).thenReturn(plan);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rechargeService.initiateRecharge(userId, buildRequest()));

        assertEquals("Invalid Operator or Plan", ex.getMessage());
        verifyNoInteractions(paymentClient, rechargeRepository);
    }

    @Test
    void testInitiateRecharge_InvalidOperator() {
        Long userId = 501L;
        when(userClient.getUserById(userId)).thenReturn(new UserDto());
        when(operatorClient.getOperatorById(101L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> rechargeService.initiateRecharge(userId, buildRequest()));
    }

    @Test
    void testInitiateRecharge_PlanNotFound() {
        Long userId = 501L;
        when(userClient.getUserById(userId)).thenReturn(new UserDto());
        when(operatorClient.getOperatorById(101L)).thenReturn(new OperatorDto(101L, "Airtel"));
        when(operatorClient.getPlanById(201L)).thenReturn(null); // Plan not found branch

        assertThrows(RuntimeException.class, () -> rechargeService.initiateRecharge(userId, buildRequest()));
    }

    @Test
    void testGetRechargeById_Found() {

        Recharge recharge = buildRecharge(1L, "SUCCESS");
        when(rechargeRepository.findById(1L)).thenReturn(Optional.of(recharge));

        RechargeResponse response = rechargeService.getRechargeById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("SUCCESS", response.getStatus());
    }

    @Test
    void testGetRechargeById_NotFound() {

        when(rechargeRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rechargeService.getRechargeById(99L));

        assertEquals("Recharge not found", ex.getMessage());
    }

    @Test
    void testGetRechargesByUserId() {

        Recharge r1 = buildRecharge(1L, "SUCCESS");
        Recharge r2 = buildRecharge(2L, "FAILED");
        when(rechargeRepository.findByUserId(501L)).thenReturn(List.of(r1, r2));

        List<RechargeResponse> responses = rechargeService.getRechargesByUserId(501L);

        assertEquals(2, responses.size());
        assertEquals("SUCCESS", responses.get(0).getStatus());
        assertEquals("FAILED",  responses.get(1).getStatus());
    }

    @Test
    void testGetAllRecharges() {
        Recharge r = buildRecharge(1L, "SUCCESS");
        when(rechargeRepository.findAll()).thenReturn(List.of(r));

        List<RechargeResponse> responses = rechargeService.getAllRecharges();
        assertEquals(1, responses.size());
        verify(rechargeRepository).findAll();
    }

    // ── FALLBACK TESTS ───────────────────────────────────────────────────────

    @Test
    void testPaymentFallback() {
        PaymentRequest request = new PaymentRequest();
        PaymentResponse response = rechargeService.paymentFallback(request, new RuntimeException("Down"));
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("unavailable"));
    }

    @Test
    void testUserFallback() {
        assertNull(rechargeService.userFallback(1L, new RuntimeException("Down")));
    }

    @Test
    void testOperatorFallback() {
        assertNull(rechargeService.operatorFallback(1L, new RuntimeException("Down")));
    }

    @Test
    void testPlanFallback() {
        assertNull(rechargeService.planFallback(1L, new RuntimeException("Down")));
    }

    @Test
    void testCallPaymentService() {
        PaymentRequest req = new PaymentRequest();
        PaymentResponse resp = new PaymentResponse();
        resp.setStatus("SUCCESS");
        when(paymentClient.processPayment(req)).thenReturn(resp);

        PaymentResponse result = rechargeService.callPaymentService(req);
        assertEquals("SUCCESS", result.getStatus());
    }
}
