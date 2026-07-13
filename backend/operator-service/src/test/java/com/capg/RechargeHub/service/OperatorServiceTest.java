/**
 * ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : OperatorServiceTest
 * DESCRIPTION:
 *   This class is used to test the business logic of OperatorService.
 *   Mockito is used to mock repository dependencies so that no real
 *   database connection is required during testing.
 * ================================================================
 */
package com.capg.RechargeHub.service;

// JUnit 5
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

// Mockito
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// DTO
import com.capg.RechargeHub.dto.OperatorDto;
import com.capg.RechargeHub.dto.PlanDto;

// Entity
import com.capg.RechargeHub.entity.Operator;
import com.capg.RechargeHub.entity.Plan;

// Repository
import com.capg.RechargeHub.repository.OperatorRepository;
import com.capg.RechargeHub.repository.PlanRepository;

// Service
import com.capg.RechargeHub.service.CloudinaryService;

// Java
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.mock.web.MockMultipartFile;

// Static imports
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class contains unit tests for OperatorService methods.
 * Each test verifies a specific functionality like CRUD operations.
 * Both success and failure scenarios are covered for robustness.
 * Mockito helps isolate service logic from database dependencies.
 */
@ExtendWith(MockitoExtension.class)
class OperatorServiceTest {

    @Mock
    private OperatorRepository operatorRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private OperatorService operatorService;

    private Operator operator;
    private Plan plan;

    /**
     * Initializes test data before each test execution.
     * Creates sample Operator and Plan objects.
     * These objects are reused across multiple test cases.
     * Helps in maintaining consistency in test scenarios.
     */
    @BeforeEach
    void setUp() {
        operator = new Operator();
        operator.setId(1L);
        operator.setName("Jio");
        operator.setType("Prepaid");
        operator.setCircle("India");

        plan = new Plan();
        plan.setId(1L);
        plan.setAmount(199.0);
        plan.setValidity("28 Days");
        plan.setDescription("Unlimited Calls");
        plan.setOperator(operator);
    }

    /**
     * Tests retrieval of all operators from the system.
     * Mocks repository to return a predefined list.
     * Verifies that the service correctly maps entities to DTOs.
     * Ensures list size and data correctness.
     */
    @Test
    void testGetAllOperators() {
        when(operatorRepository.findAll()).thenReturn(List.of(operator));

        List<OperatorDto> result = operatorService.getAllOperators();

        assertEquals(1, result.size());
        assertEquals("Jio", result.get(0).getName());
    }

    /**
     * Tests successful retrieval of operator by ID.
     * Repository returns a valid operator object.
     * Service converts entity into DTO correctly.
     * Verifies expected operator name.
     */
    @Test
    void testGetOperatorById_Success() {
        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));

        OperatorDto result = operatorService.getOperatorById(1L);

        assertEquals("Jio", result.getName());
    }

    /**
     * Tests scenario when operator is not found in database.
     * Repository returns empty Optional.
     * Service should throw RuntimeException.
     * Validates exception handling logic.
     */
    @Test
    void testGetOperatorById_NotFound() {
        when(operatorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            operatorService.getOperatorById(1L);
        });
    }

    /**
     * Tests creation of a new operator.
     * Mocks save operation of repository.
     * Verifies that operator is saved successfully.
     * Also checks if returned DTO contains expected values.
     */
    @Test
    void testCreateOperator() {
        OperatorDto dto = new OperatorDto();
        dto.setName("Airtel");
        dto.setType("Postpaid");
        dto.setCircle("India");

        when(operatorRepository.save(any(Operator.class))).thenReturn(operator);

        OperatorDto result = operatorService.createOperator(dto);

        assertEquals("Jio", result.getName());
        verify(operatorRepository, times(1)).save(any(Operator.class));
    }

    /**
     * Tests updating an existing operator.
     * Repository returns existing operator and saves updated version.
     * Service updates fields and returns updated DTO.
     * Verifies updated name field.
     */
    @Test
    void testUpdateOperator() {
        OperatorDto dto = new OperatorDto();
        dto.setName("Updated");
        dto.setType("Prepaid");
        dto.setCircle("India");

        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));
        when(operatorRepository.save(any(Operator.class))).thenReturn(operator);

        OperatorDto result = operatorService.updateOperator(1L, dto);

        assertEquals("Updated", result.getName());
    }

    /**
     * Tests update operation when operator does not exist.
     * Repository returns empty Optional.
     * Service should throw RuntimeException.
     * Ensures proper error handling.
     */
    @Test
    void testUpdateOperator_NotFound() {
        when(operatorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            operatorService.updateOperator(1L, new OperatorDto());
        });
    }

    /**
     * Tests deletion of an existing operator.
     * Repository returns operator which is then deleted.
     * Verifies that delete method is called exactly once.
     * Ensures correct interaction with repository.
     */
    @Test
    void testDeleteOperator() {
        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));

        operatorService.deleteOperator(1L);

        verify(operatorRepository, times(1)).delete(operator);
    }

    /**
     * Tests deletion when operator is not found.
     * Repository returns empty Optional.
     * Service throws RuntimeException.
     * Validates failure scenario.
     */
    @Test
    void testDeleteOperator_NotFound() {
        when(operatorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            operatorService.deleteOperator(1L);
        });
    }

    /**
     * Tests retrieval of a plan by ID.
     * Repository returns valid plan object.
     * Service converts entity into DTO.
     * Verifies plan amount correctness.
     */
    @Test
    void testGetPlanById() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

        PlanDto result = operatorService.getPlanById(1L);

        assertEquals(199.0, result.getAmount());
    }

    /**
     * Tests scenario where plan is not found.
     * Repository returns empty Optional.
     * Service throws RuntimeException.
     * Ensures proper exception handling.
     */
    @Test
    void testGetPlanById_NotFound() {
        when(planRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            operatorService.getPlanById(1L);
        });
    }

    /**
     * Tests creation of a new plan.
     * Mocks operator lookup and plan save operation.
     * Verifies that plan is saved correctly.
     * Ensures correct mapping to DTO.
     */
    @Test
    void testCreatePlan() {
        PlanDto dto = new PlanDto();
        dto.setAmount(299.0);
        dto.setValidity("56 Days");
        dto.setDescription("Data Plan");

        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));
        when(planRepository.save(any(Plan.class))).thenReturn(plan);

        PlanDto result = operatorService.createPlan(1L, dto);

        assertEquals(199.0, result.getAmount());
        verify(planRepository, times(1)).save(any(Plan.class));
    }

    /**
     * Tests plan creation when operator does not exist.
     * Repository returns empty Optional.
     * Service throws RuntimeException.
     * Ensures validation before creation.
     */
    @Test
    void testCreatePlan_OperatorNotFound() {
        when(operatorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            operatorService.createPlan(1L, new PlanDto());
        });
    }

    /**
     * Tests deletion of a plan.
     * Repository returns plan which is then deleted.
     * Verifies delete method invocation.
     * Ensures proper repository interaction.
     */
    @Test
    void testDeletePlan() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

        operatorService.deletePlan(1L);

        verify(planRepository, times(1)).delete(plan);
    }

    /**
     * Tests updatePlan success scenario.
     * Repository returns existing plan and saves updated version.
     * Verifies updated amount field.
     */
    @Test
    void testUpdatePlan() {
        PlanDto dto = new PlanDto();
        dto.setAmount(399.0);
        dto.setValidity("84 Days");
        dto.setDescription("Premium Plan");

        Plan updatedPlan = new Plan();
        updatedPlan.setId(1L);
        updatedPlan.setAmount(399.0);
        updatedPlan.setValidity("84 Days");
        updatedPlan.setDescription("Premium Plan");
        updatedPlan.setOperator(operator);

        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenReturn(updatedPlan);

        PlanDto result = operatorService.updatePlan(1L, dto);

        assertEquals(399.0, result.getAmount());
        verify(planRepository, times(1)).save(any(Plan.class));
    }

    /**
     * Tests updatePlan when plan is not found.
     * Repository returns empty Optional.
     * Service throws RuntimeException.
     */
    @Test
    void testUpdatePlan_NotFound() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            operatorService.updatePlan(99L, new PlanDto());
        });
    }

    /**
     * Tests deletePlan when plan is not found.
     * Repository returns empty Optional.
     * Service throws RuntimeException.
     */
    @Test
    void testDeletePlan_NotFound() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            operatorService.deletePlan(99L);
        });
    }

    /**
     * Tests updateOperatorLogo success scenario.
     * Mocks Cloudinary upload and operator repository.
     * Verifies logo URL is returned and operator is saved.
     */
    @Test
    void testUpdateOperatorLogo_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("logo", "logo.png",
                "image/png", "fake-image".getBytes());

        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));
        when(cloudinaryService.uploadImage(file)).thenReturn("http://cloudinary.com/logo.png");
        when(operatorRepository.save(any(Operator.class))).thenReturn(operator);

        String url = operatorService.updateOperatorLogo(1L, file);

        assertEquals("http://cloudinary.com/logo.png", url);
        verify(operatorRepository, times(1)).save(any(Operator.class));
    }

    /**
     * Tests updateOperatorLogo when operator is not found.
     * Repository returns empty Optional.
     * Service throws RuntimeException.
     */
    @Test
    void testUpdateOperatorLogo_NotFound() {
        MockMultipartFile file = new MockMultipartFile("logo", "logo.png",
                "image/png", "fake-image".getBytes());

        when(operatorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            operatorService.updateOperatorLogo(99L, file);
        });
    }
    /**
     * Tests mapping of a plan that has no associated operator.
     * Ensures that the operatorId in DTO is null and doesn't crash.
     */
    @Test
    void testMapToPlanDto_NullOperator() {
        Plan orphanPlan = new Plan();
        orphanPlan.setId(10L);
        orphanPlan.setOperator(null); // No operator

        when(planRepository.findById(10L)).thenReturn(Optional.of(orphanPlan));
        PlanDto result = operatorService.getPlanById(10L);

        assertNull(result.getOperatorId());
    }

    /**
     * Tests mapping of an operator that has no associated plans.
     * Ensures that the plans list in DTO is null or empty and doesn't crash.
     */
    @Test
    void testMapToOperatorDto_NullPlans() {
        Operator soloOp = new Operator();
        soloOp.setId(2L);
        soloOp.setPlans(null); // No plans list

        when(operatorRepository.findById(2L)).thenReturn(Optional.of(soloOp));
        OperatorDto result = operatorService.getOperatorById(2L);

        assertNull(result.getPlans());
    }

    /**
     * Tests the catch block in getAllOperators by forcing a repository exception.
     */
    @Test
    void testGetAllOperators_Exception() {
        when(operatorRepository.findAll()).thenThrow(new RuntimeException("Database Down"));
        assertThrows(RuntimeException.class, () -> operatorService.getAllOperators());
    }

    /**
     * Tests the catch block in createOperator by forcing a repository exception.
     */
    @Test
    void testCreateOperator_Exception() {
        when(operatorRepository.save(any())).thenThrow(new RuntimeException("Save Failed"));
        assertThrows(RuntimeException.class, () -> operatorService.createOperator(new OperatorDto()));
    }

    @Test
    void testMapToOperatorDto_WithPlans() {
        Operator op = new Operator();
        op.setId(1L);
        op.setName("Jio");
        Plan p = new Plan();
        p.setId(101L);
        p.setOperator(op);
        op.setPlans(List.of(p));
        when(operatorRepository.findById(1L)).thenReturn(Optional.of(op));
        OperatorDto result = operatorService.getOperatorById(1L);
        assertNotNull(result.getPlans());
        assertEquals(1, result.getPlans().size());
    }

    @Test
    void testGetOperatorById_Exception() {
        when(operatorRepository.findById(anyLong())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> operatorService.getOperatorById(1L));
    }

    @Test
    void testUpdateOperator_Exception() {
        when(operatorRepository.findById(anyLong())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> operatorService.updateOperator(1L, new OperatorDto()));
    }

    @Test
    void testDeleteOperator_Exception() {
        when(operatorRepository.findById(anyLong())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> operatorService.deleteOperator(1L));
    }

    @Test
    void testGetPlanById_Exception() {
        when(planRepository.findById(anyLong())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> operatorService.getPlanById(1L));
    }

    @Test
    void testCreatePlan_Exception() {
        when(operatorRepository.findById(anyLong())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> operatorService.createPlan(1L, new PlanDto()));
    }

    @Test
    void testUpdatePlan_Exception() {
        when(planRepository.findById(anyLong())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> operatorService.updatePlan(1L, new PlanDto()));
    }

    @Test
    void testDeletePlan_Exception() {
        when(planRepository.findById(anyLong())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> operatorService.deletePlan(1L));
    }
}