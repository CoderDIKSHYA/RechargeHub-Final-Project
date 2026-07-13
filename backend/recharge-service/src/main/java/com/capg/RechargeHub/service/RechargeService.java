package com.capg.RechargeHub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.capg.RechargeHub.client.*;
import com.capg.RechargeHub.dto.*;
import com.capg.RechargeHub.entity.Recharge;
import com.capg.RechargeHub.repository.RechargeRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.List;
import java.util.stream.Collectors;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : RechargeService
 * DESCRIPTION:
 *   Service layer for the Recharge Service microservice.
 *   Includes Resilience4j Circuit Breaker for payment processing.
 * ================================================================ */
@Slf4j
@Service
public class RechargeService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RechargeService.class);

    private final RechargeRepository rechargeRepository;
    private final UserClient userClient;
    private final OperatorClient operatorClient;
    private final PaymentClient paymentClient;

    public RechargeService(RechargeRepository rechargeRepository,
                           UserClient userClient,
                           OperatorClient operatorClient,
                           PaymentClient paymentClient) {
        this.rechargeRepository = rechargeRepository;
        this.userClient = userClient;
        this.operatorClient = operatorClient;
        this.paymentClient = paymentClient;
    }

    public RechargeResponse initiateRecharge(Long userId, RechargeRequest request) {

        logger.info("Starting recharge for userId: {}", userId);

        // Fetch user, operator, and plan synchronously to ensure token propagation
        // Fetch user, operator, and plan with circuit breakers
        // Fetch user first
        UserDto user = getUserSecurely(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Fetch operator and plan
        OperatorDto operator = getOperatorSecurely(request.getOperatorId());
        PlanDto plan = getPlanSecurely(request.getPlanId());

        if (operator == null || plan == null || !plan.getOperatorId().equals(operator.getId())) {
            throw new RuntimeException("Invalid Operator or Plan");
        }

        Recharge recharge = new Recharge(
                userId,
                operator.getId(),
                plan.getId(),
                request.getMobileNumber(),
                plan.getAmount(),
                "PENDING"
        );
        recharge = rechargeRepository.save(recharge);

        recharge = rechargeRepository.save(recharge);
        logger.info("Pending recharge created for id: {}", recharge.getId());
        return mapToResponse(recharge, "PENDING");
    }

    public RechargeResponse updateRechargeStatus(Long id, String status, String transactionId) {
        logger.info("Updating recharge id={} to status={} txnId={}", id, status, transactionId);
        Recharge recharge = rechargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recharge not found"));
        
        recharge.setStatus(status);
        if (transactionId != null) {
            recharge.setTransactionId(transactionId);
        }
        recharge = rechargeRepository.save(recharge);
        
        if ("SUCCESS".equalsIgnoreCase(status)) {
            logger.info("📡 TELCO ACTIVATION: Successfully pushed recharge plan activation for mobile: {}", recharge.getMobileNumber());
        }
        
        return mapToResponse(recharge, "Status updated to " + status);
    }

    @CircuitBreaker(name = "paymentServiceCB", fallbackMethod = "paymentFallback")
    @io.github.resilience4j.retry.annotation.Retry(name = "paymentRetry")
    public PaymentResponse callPaymentService(PaymentRequest paymentRequest) {
        return paymentClient.processPayment(paymentRequest);
    }

    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "userFallback")
    public UserDto getUserSecurely(Long userId) {
        return userClient.getUserById(userId);
    }

    @CircuitBreaker(name = "operatorServiceCB", fallbackMethod = "operatorFallback")
    public OperatorDto getOperatorSecurely(Long operatorId) {
        return operatorClient.getOperatorById(operatorId);
    }

    @CircuitBreaker(name = "operatorServiceCB", fallbackMethod = "planFallback")
    public PlanDto getPlanSecurely(Long planId) {
        return operatorClient.getPlanById(planId);
    }

    /** Fallback method for payment service failures */
    public PaymentResponse paymentFallback(PaymentRequest request, Throwable t) {
        logger.error("RESILIENCE: Payment service unavailable. Reason: {}", t.getMessage());
        PaymentResponse response = new PaymentResponse();
        response.setStatus("FAILED");
        response.setMessage("Payment system currently unavailable. Please try again later.");
        return response;
    }

    public UserDto userFallback(Long userId, Throwable t) {
        logger.error("RESILIENCE: User service unavailable for ID {}. Reason: {}", userId, t.getMessage());
        return null; // Will trigger the "User not found" check in initiateRecharge
    }

    public OperatorDto operatorFallback(Long operatorId, Throwable t) {
        logger.error("RESILIENCE: Operator service unavailable for ID {}. Reason: {}", operatorId, t.getMessage());
        return null;
    }

    public PlanDto planFallback(Long planId, Throwable t) {
        logger.error("RESILIENCE: Plan service unavailable for ID {}. Reason: {}", planId, t.getMessage());
        return null;
    }

    public RechargeResponse getRechargeById(Long id) {
        Recharge recharge = rechargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recharge not found"));
        return mapToResponse(recharge, "Success");
    }

    public List<RechargeResponse> getRechargesByUserId(Long userId) {
        return rechargeRepository.findByUserId(userId).stream()
                .map(r -> mapToResponse(r, "Success"))
                .collect(Collectors.toList());
    }

    public List<RechargeResponse> getAllRecharges() {
        return rechargeRepository.findAll().stream()
                .map(r -> mapToResponse(r, "Success"))
                .collect(Collectors.toList());
    }

    private RechargeResponse mapToResponse(Recharge recharge, String message) {
        RechargeResponse response = new RechargeResponse(
                recharge.getId(),
                recharge.getUserId(),
                recharge.getOperatorId(),
                recharge.getPlanId(),
                recharge.getMobileNumber(),
                recharge.getAmount(),
                recharge.getStatus(),
                recharge.getCreatedAt(),
                message
        );
        response.setTransactionId(recharge.getTransactionId());
        return response;
    }
}
