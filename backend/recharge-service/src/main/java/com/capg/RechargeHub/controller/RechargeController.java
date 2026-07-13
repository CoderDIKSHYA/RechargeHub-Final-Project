package com.capg.RechargeHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capg.RechargeHub.dto.RechargeRequest;
import com.capg.RechargeHub.dto.RechargeResponse;
import com.capg.RechargeHub.service.RechargeService;

import java.util.List;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : RechargeController
 * DESCRIPTION:
 *   REST controller for the Recharge Service.
 *   Exposes APIs for initiating mobile recharges and retrieving
 *   recharge records by ID or by user ID.
 *   Delegates all business logic to RechargeService.
 *   Swagger annotations are applied for API documentation.
 * ================================================================ */
@RestController
@RequestMapping("/recharges")
@Tag(name = "Recharge API", description = "APIs for handling mobile recharge operations")
public class RechargeController {

    /** Logger instance for tracking API calls and debugging */
    private static final Logger logger = LoggerFactory.getLogger(RechargeController.class);

    private final RechargeService rechargeService;

    /* ================================================================
     * CONSTRUCTOR: RechargeController
     * DESCRIPTION:
     *   Constructor injection of RechargeService.
     *   Preferred over field injection for testability with Mockito.
     * ================================================================ */
    public RechargeController(RechargeService rechargeService) {
        this.rechargeService = rechargeService;
    }

    /* ================================================================
     * METHOD: initiateRecharge
     * DESCRIPTION:
     *   Accepts a recharge request along with the user ID from the
     *   request header. Validates the request body and delegates
     *   processing to the service layer. Returns the recharge result.
     * ================================================================ */
    @Operation(
        summary = "Initiate Recharge",
        description = "Creates a new recharge request and processes payment using user ID from header"
    )
    @ApiResponse(responseCode = "200", description = "Recharge successful")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping
    public ResponseEntity<RechargeResponse> initiateRecharge(
            @Parameter(description = "User ID from header", required = true)
            @RequestHeader(value = "X-User-Id", required = true) Long userId,
            @Valid @RequestBody RechargeRequest request) {

        logger.info("Initiating recharge for userId: {}", userId);
        return ResponseEntity.ok(rechargeService.initiateRecharge(userId, request));
    }

    /* ================================================================
     * METHOD: getRechargeById
     * DESCRIPTION:
     *   Fetches a single recharge record by its unique ID.
     *   Returns 404 if the recharge does not exist.
     * ================================================================ */
    @Operation(
        summary = "Get Recharge by ID",
        description = "Fetch recharge details using recharge ID"
    )
    @ApiResponse(responseCode = "200", description = "Recharge found")
    @ApiResponse(responseCode = "404", description = "Recharge not found")
    @GetMapping("/{id}")
    public ResponseEntity<RechargeResponse> getRechargeById(@PathVariable @Min(1) Long id) {
        logger.info("Fetching recharge by id: {}", id);
        return ResponseEntity.ok(rechargeService.getRechargeById(id));
    }

    /* ================================================================
     * METHOD: getRechargesByUserId
     * DESCRIPTION:
     *   Returns all recharge records associated with a given user ID.
     *   Useful for displaying recharge history on the user dashboard.
     * ================================================================ */
    @Operation(
        summary = "Get Recharges by User",
        description = "Fetch all recharge records for a specific user"
    )
    @ApiResponse(responseCode = "200", description = "Recharges fetched successfully")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RechargeResponse>> getRechargesByUserId(@PathVariable @Min(1) Long userId) {
        logger.info("Fetching recharges for userId: {}", userId);
        return ResponseEntity.ok(rechargeService.getRechargesByUserId(userId));
    }

    @Operation(summary = "Get all Recharges [ADMIN]", description = "Fetch all recharge records across the platform")
    @GetMapping("/admin/all")
    public ResponseEntity<List<RechargeResponse>> getAllRecharges() {
        logger.info("Admin fetching all recharges");
        return ResponseEntity.ok(rechargeService.getAllRecharges());
    }

    @Operation(summary = "Update Status", description = "Update the status of a recharge record")
    @PutMapping("/{id}/status")
    public ResponseEntity<RechargeResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String transactionId) {
        logger.info("Updating status for recharge ID: {} to {}", id, status);
        return ResponseEntity.ok(rechargeService.updateRechargeStatus(id, status, transactionId));
    }
}
