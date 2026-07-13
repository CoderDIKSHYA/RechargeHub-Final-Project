/**
 * ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : OperatorService
 * DESCRIPTION:
 *   This service layer class handles all business logic related to
 *   operators and recharge plans. It interacts with repositories
 *   and converts entities into DTOs for safe API responses.
 * ================================================================
 */
package com.capg.RechargeHub.service;

// Spring
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

// Logging
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Java
import java.util.List;
import java.util.stream.Collectors;

// DTO
import com.capg.RechargeHub.dto.OperatorDto;
import com.capg.RechargeHub.dto.PlanDto;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


// Entity
import com.capg.RechargeHub.entity.Operator;
import com.capg.RechargeHub.entity.Plan;

// Repository
import com.capg.RechargeHub.repository.OperatorRepository;
import com.capg.RechargeHub.repository.PlanRepository;

@Service
public class OperatorService {

    private static final Logger logger = LogManager.getLogger(OperatorService.class);

    private final OperatorRepository operatorRepository;
    private final PlanRepository planRepository;
    private final CloudinaryService cloudinaryService;

    public OperatorService(OperatorRepository operatorRepository,
                           PlanRepository planRepository,
                           CloudinaryService cloudinaryService) {
        this.operatorRepository = operatorRepository;
        this.planRepository = planRepository;
        this.cloudinaryService = cloudinaryService;
    }


    /**
     * Retrieves all operators from the database.
     * Converts each Operator entity into OperatorDto using stream mapping.
     * This ensures that only required fields are exposed in API response.
     * Useful for displaying all available operators in UI.
     */
    @Cacheable(value = "operators")
    public List<OperatorDto> getAllOperators() {
        logger.info("Entering getAllOperators");
        try {
            List<OperatorDto> result = operatorRepository.findAll()
                    .stream()
                    .map(this::mapToOperatorDto)
                    .collect(Collectors.toList());

            logger.info("Returned {} operators", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error in getAllOperators", e);
            throw e;
        }
    }

    /**
     * Fetches a specific operator using its unique ID.
     * If the operator is not found, a RuntimeException is thrown.
     * Converts the entity into DTO before returning to controller.
     * Used when detailed information of a single operator is required.
     */
    @Cacheable(value = "operators", key = "#id")
    public OperatorDto getOperatorById(Long id) {
        logger.info("Entering getOperatorById id={}", id);
        try {
            Operator operator = operatorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Operator not found"));

            return mapToOperatorDto(operator);
        } catch (Exception e) {
            logger.error("Error in getOperatorById id={}", id, e);
            throw e;
        }
    }

    /**
     * Creates a new operator in the system.
     * Takes OperatorDto as input and converts it into an entity object.
     * Saves the entity into database using repository layer.
     * Returns the saved operator as DTO for confirmation.
     */
    @CacheEvict(value = "operators", allEntries = true)
    public OperatorDto createOperator(OperatorDto operatorDto) {
        logger.info("Creating operator {}", operatorDto);
        try {
            Operator operator = new Operator();
            operator.setName(operatorDto.getName());
            operator.setType(operatorDto.getType());
            operator.setCircle(operatorDto.getCircle());

            Operator saved = operatorRepository.save(operator);
            return mapToOperatorDto(saved);
        } catch (Exception e) {
            logger.error("Error in createOperator", e);
            throw e;
        }
    }

    /**
     * Updates an existing operator based on given ID.
     * First retrieves the operator, then updates its fields.
     * Saves updated entity back to database.
     * Returns updated operator data in DTO format.
     */
    @CacheEvict(value = "operators", allEntries = true)
    public OperatorDto updateOperator(Long id, OperatorDto operatorDto) {
        logger.info("Updating operator id={}", id);
        try {
            Operator operator = operatorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Operator not found"));

            operator.setName(operatorDto.getName());
            operator.setType(operatorDto.getType());
            operator.setCircle(operatorDto.getCircle());

            return mapToOperatorDto(operatorRepository.save(operator));
        } catch (Exception e) {
            logger.error("Error in updateOperator id={}", id, e);
            throw e;
        }
    }

    /**
     * Deletes an operator from the system using its ID.
     * First checks if the operator exists in database.
     * If found, deletes the operator permanently.
     * Used for administrative operations and data cleanup.
     */
    @CacheEvict(value = "operators", allEntries = true)
    public void deleteOperator(Long id) {
        logger.info("Deleting operator id={}", id);
        try {
            Operator operator = operatorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Operator not found"));

            operatorRepository.delete(operator);
        } catch (Exception e) {
            logger.error("Error in deleteOperator id={}", id, e);
            throw e;
        }
    }

    /**
     * Retrieves a recharge plan using its unique ID.
     * If plan does not exist, throws an exception.
     * Converts entity into DTO before returning.
     * Useful for viewing detailed plan information.
     */
    public PlanDto getPlanById(Long id) {
        logger.info("Fetching plan id={}", id);
        try {
            Plan plan = planRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            return mapToPlanDto(plan);
        } catch (Exception e) {
            logger.error("Error in getPlanById id={}", id, e);
            throw e;
        }
    }

    /**
     * Creates a new recharge plan for a specific operator.
     * Associates plan with operator using operatorId.
     * Saves the plan into database via repository.
     * Returns created plan as DTO.
     */
    @CacheEvict(value = "operators", allEntries = true)
    public PlanDto createPlan(Long operatorId, PlanDto planDto) {
        logger.info("Creating plan for operatorId={}", operatorId);
        try {
            Operator operator = operatorRepository.findById(operatorId)
                    .orElseThrow(() -> new RuntimeException("Operator not found"));

            Plan plan = new Plan();
            plan.setAmount(planDto.getAmount());
            plan.setValidity(planDto.getValidity());
            plan.setData(planDto.getData());
            plan.setType(planDto.getType());
            plan.setDescription(planDto.getDescription());
            plan.setOperator(operator);

            return mapToPlanDto(planRepository.save(plan));
        } catch (Exception e) {
            logger.error("Error in createPlan operatorId={}", operatorId, e);
            throw e;
        }
    }

    /**
     * Updates an existing recharge plan using plan ID.
     * Fetches plan, modifies fields like amount and validity.
     * Saves updated plan back into database.
     * Returns updated plan in DTO format.
     */
    @CacheEvict(value = "operators", allEntries = true)
    public PlanDto updatePlan(Long planId, PlanDto planDto) {
        logger.info("Updating plan id={}", planId);
        try {
            Plan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            plan.setAmount(planDto.getAmount());
            plan.setValidity(planDto.getValidity());
            plan.setData(planDto.getData());
            plan.setType(planDto.getType());
            plan.setDescription(planDto.getDescription());

            return mapToPlanDto(planRepository.save(plan));
        } catch (Exception e) {
            logger.error("Error in updatePlan id={}", planId, e);
            throw e;
        }
    }

    /**
     * Deletes a recharge plan using its unique ID.
     * Ensures the plan exists before deletion.
     * Removes plan from database permanently.
     * Used for maintenance and admin-level operations.
     */
    @CacheEvict(value = "operators", allEntries = true)
    public void deletePlan(Long planId) {
        logger.info("Deleting plan id={}", planId);
        try {
            Plan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            planRepository.delete(plan);
        } catch (Exception e) {
            logger.error("Error in deletePlan id={}", planId, e);
            throw e;
        }
    }

    /**
     * Converts Operator entity into OperatorDto.
     * Maps all basic fields like id, name, type, and circle.
     * Also converts associated plans into PlanDto list.
     * Used internally for safe API response transformation.
     */
    private OperatorDto mapToOperatorDto(Operator operator) {
        OperatorDto dto = new OperatorDto();

        dto.setId(operator.getId());
        dto.setName(operator.getName());
        dto.setType(operator.getType());
        dto.setCircle(operator.getCircle());
        dto.setLogoUrl(operator.getLogoUrl());

        if (operator.getPlans() != null) {
            dto.setPlans(operator.getPlans()
                    .stream()
                    .map(this::mapToPlanDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @CacheEvict(value = "operators", allEntries = true)
    public String updateOperatorLogo(Long operatorId, MultipartFile file) throws IOException {
        logger.info("Updating logo for operator id={}", operatorId);
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Operator not found"));

        String url = cloudinaryService.uploadImage(file);
        operator.setLogoUrl(url);
        operatorRepository.save(operator);
        return url;
    }


    /**
     * Converts Plan entity into PlanDto.
     * Maps fields like amount, validity, and description.
     * Extracts operatorId from associated operator entity.
     * Used internally for consistent response formatting.
     */
    private PlanDto mapToPlanDto(Plan plan) {
        PlanDto dto = new PlanDto();

        dto.setId(plan.getId());
        dto.setOperatorId(
                plan.getOperator() != null ? plan.getOperator().getId() : null
        );
        dto.setAmount(plan.getAmount());
        dto.setValidity(plan.getValidity());
        dto.setData(plan.getData());
        dto.setType(plan.getType());
        dto.setDescription(plan.getDescription());

        return dto;
    }
}