/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : OperatorControllerTest
 * DESCRIPTION:
 *   Unit test class for OperatorController.
 *   Uses MockMvc standalone setup to test HTTP layer without
 *   loading the full Spring context or requiring Oracle/Eureka.
 *   Mocks OperatorService to isolate controller behavior.
 *   Tests cover GET /operators, GET /operators/{id},
 *   POST /operators, PUT /operators/{id}, DELETE /operators/{id},
 *   POST /operators/{id}/plans, PUT /plans/{id}, DELETE /plans/{id}.
 * ================================================================ */
package com.capg.RechargeHub.controller;

import com.capg.RechargeHub.dto.OperatorDto;
import com.capg.RechargeHub.dto.PlanDto;
import com.capg.RechargeHub.service.OperatorService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class OperatorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OperatorService operatorService;

    @InjectMocks
    private OperatorController operatorController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(operatorController).build();
        objectMapper = new ObjectMapper();
    }

    private OperatorDto buildOperatorDto() {
        OperatorDto dto = new OperatorDto();
        dto.setId(1L);
        dto.setName("Jio");
        dto.setType("Prepaid");
        dto.setCircle("India");
        return dto;
    }

    private PlanDto buildPlanDto() {
        PlanDto dto = new PlanDto();
        dto.setId(1L);
        dto.setOperatorId(1L);
        dto.setAmount(199.0);
        dto.setValidity("28 Days");
        dto.setDescription("Unlimited Calls");
        return dto;
    }

    // ✅ GET /operators
    @Test
    void testGetAllOperators_Returns200() throws Exception {
        when(operatorService.getAllOperators()).thenReturn(List.of(buildOperatorDto()));

        mockMvc.perform(get("/operators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Jio"));
    }

    // ✅ GET /operators/{id}
    @Test
    void testGetOperatorById_Returns200() throws Exception {
        when(operatorService.getOperatorById(1L)).thenReturn(buildOperatorDto());

        mockMvc.perform(get("/operators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Jio"));
    }

    // ✅ POST /operators
    @Test
    void testCreateOperator_Returns200() throws Exception {
        OperatorDto request = new OperatorDto();
        request.setName("Airtel");
        request.setType("Postpaid");
        request.setCircle("India");

        when(operatorService.createOperator(any())).thenReturn(buildOperatorDto());

        mockMvc.perform(post("/operators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jio"));
    }

    // ✅ PUT /operators/{id}
    @Test
    void testUpdateOperator_Returns200() throws Exception {
        OperatorDto request = new OperatorDto();
        request.setName("Updated");
        request.setType("Prepaid");
        request.setCircle("India");

        OperatorDto updated = buildOperatorDto();
        updated.setName("Updated");

        when(operatorService.updateOperator(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/operators/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    // ✅ DELETE /operators/{id}
    @Test
    void testDeleteOperator_Returns204() throws Exception {
        doNothing().when(operatorService).deleteOperator(1L);

        mockMvc.perform(delete("/operators/1"))
                .andExpect(status().isNoContent());

        verify(operatorService, times(1)).deleteOperator(1L);
    }

    // ✅ GET /operators/plans/{id}
    @Test
    void testGetPlanById_Returns200() throws Exception {
        when(operatorService.getPlanById(1L)).thenReturn(buildPlanDto());

        mockMvc.perform(get("/operators/plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(199.0));
    }

    // ✅ POST /operators/{operatorId}/plans
    @Test
    void testCreatePlan_Returns200() throws Exception {
        PlanDto request = new PlanDto();
        request.setAmount(299.0);
        request.setValidity("56 Days");
        request.setDescription("Data Plan");

        when(operatorService.createPlan(eq(1L), any())).thenReturn(buildPlanDto());

        mockMvc.perform(post("/operators/1/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(199.0));
    }

    // ✅ PUT /operators/plans/{id}
    @Test
    void testUpdatePlan_Returns200() throws Exception {
        PlanDto request = new PlanDto();
        request.setAmount(399.0);
        request.setValidity("84 Days");
        request.setDescription("Premium Plan");

        PlanDto updated = buildPlanDto();
        updated.setAmount(399.0);

        when(operatorService.updatePlan(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/operators/plans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(399.0));
    }

    // ✅ DELETE /operators/plans/{id}
    @Test
    void testDeletePlan_Returns204() throws Exception {
        doNothing().when(operatorService).deletePlan(1L);

        mockMvc.perform(delete("/operators/plans/1"))
                .andExpect(status().isNoContent());

        verify(operatorService, times(1)).deletePlan(1L);
    }

    // ✅ POST /operators/{id}/logo - Upload Logo
    @Test
    void testUploadLogo_Returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", "fake-logo".getBytes());

        when(operatorService.updateOperatorLogo(eq(1L), any()))
                .thenReturn("http://cloudinary.com/logo.png");

        mockMvc.perform(multipart("/operators/1/logo")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logoUrl").value("http://cloudinary.com/logo.png"));
    }
}
