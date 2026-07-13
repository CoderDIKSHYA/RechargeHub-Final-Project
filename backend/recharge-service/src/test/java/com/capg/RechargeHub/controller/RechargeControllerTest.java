/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : RechargeControllerTest
 * DESCRIPTION:
 *   Unit test class for RechargeController.
 *   Uses MockMvc standalone setup to test HTTP layer without
 *   loading the full Spring context or requiring any infrastructure.
 *   Mocks RechargeService to isolate controller behavior.
 *   Tests cover POST /recharges, GET /recharges/{id},
 *   and GET /recharges/user/{userId} endpoints.
 * ================================================================ */
package com.capg.RechargeHub.controller;

import com.capg.RechargeHub.controller.RechargeController;
import com.capg.RechargeHub.dto.RechargeRequest;
import com.capg.RechargeHub.dto.RechargeResponse;
import com.capg.RechargeHub.service.RechargeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RechargeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RechargeService rechargeService;

    @InjectMocks
    private RechargeController rechargeController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(rechargeController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private RechargeResponse buildResponse(Long id, String status) {
        return new RechargeResponse(
                id, 501L, 101L, 201L,
                "9999999999", BigDecimal.valueOf(299),
                status, LocalDateTime.now(), "Recharge successful"
        );
    }

    @Test
    void testInitiateRecharge_Returns200() throws Exception {
        RechargeRequest request = new RechargeRequest();
        request.setOperatorId(101L);
        request.setPlanId(201L);
        request.setMobileNumber("9876543210");
        request.setPaymentMethod("UPI");

        when(rechargeService.initiateRecharge(eq(501L), any()))
                .thenReturn(buildResponse(1L, "PENDING"));

        mockMvc.perform(post("/recharges")
                        .header("X-User-Id", 501L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testUpdateRechargeStatus_Returns200() throws Exception {
        when(rechargeService.updateRechargeStatus(eq(1L), eq("SUCCESS"), any()))
                .thenReturn(buildResponse(1L, "SUCCESS"));

        mockMvc.perform(put("/recharges/1/status")
                        .param("status", "SUCCESS")
                        .param("transactionId", "txn_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testGetRechargeById_Returns200() throws Exception {

        when(rechargeService.getRechargeById(1L))
                .thenReturn(buildResponse(1L, "SUCCESS"));

        mockMvc.perform(get("/recharges/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testGetRechargesByUserId_Returns200() throws Exception {

        when(rechargeService.getRechargesByUserId(501L))
                .thenReturn(List.of(
                        buildResponse(1L, "SUCCESS"),
                        buildResponse(2L, "FAILED")
                ));

        mockMvc.perform(get("/recharges/user/501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[1].status").value("FAILED"));
    }

    @Test
    void testGetAllRecharges_Returns200() throws Exception {
        when(rechargeService.getAllRecharges())
                .thenReturn(List.of(buildResponse(1L, "SUCCESS")));

        mockMvc.perform(get("/recharges/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
