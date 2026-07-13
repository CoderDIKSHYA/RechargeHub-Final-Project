package com.capg.RechargeHub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.capg.RechargeHub.dto.OperatorDto;
import com.capg.RechargeHub.dto.PlanDto;
import com.capg.RechargeHub.service.OperatorService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;



@RestController
@RequestMapping("/operators")
public class OperatorController {

    private static final Logger logger = LogManager.getLogger(OperatorController.class);

    private final OperatorService operatorService;

    // ✅ Constructor Injection
    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @PostMapping("/{id}/logo")
    public ResponseEntity<Map<String, String>> uploadLogo(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file) throws IOException {
        logger.info("Uploading logo for operator id={}", id);
        String url = operatorService.updateOperatorLogo(id, file);
        return ResponseEntity.ok(Map.of("logoUrl", url));
    }


    @GetMapping
    public ResponseEntity<List<OperatorDto>> getAllOperators() {
        logger.info("Request received: getAllOperators");
        List<OperatorDto> response = operatorService.getAllOperators();
        logger.info("Returned {} operators", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperatorDto> getOperatorById(@PathVariable Long id) {
        logger.info("Request received: getOperatorById id={}", id);
        return ResponseEntity.ok(operatorService.getOperatorById(id));
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<PlanDto> getPlanById(@PathVariable Long id) {
        logger.info("Request received: getPlanById id={}", id);
        return ResponseEntity.ok(operatorService.getPlanById(id));
    }

    @PostMapping
    public ResponseEntity<OperatorDto> createOperator(@Valid @RequestBody OperatorDto operatorDto) {

        logger.info("Creating operator");
        return ResponseEntity.ok(operatorService.createOperator(operatorDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperatorDto> updateOperator(@PathVariable Long id,
                                                     @Valid @RequestBody OperatorDto operatorDto) {

        logger.info("Updating operator id={}", id);
        return ResponseEntity.ok(operatorService.updateOperator(id, operatorDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperator(@PathVariable Long id) {
        logger.info("Deleting operator id={}", id);
        operatorService.deleteOperator(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{operatorId}/plans")
    public ResponseEntity<PlanDto> createPlan(@PathVariable Long operatorId,
                                             @Valid @RequestBody PlanDto planDto) {

        logger.info("Creating plan for operatorId={}", operatorId);
        return ResponseEntity.ok(operatorService.createPlan(operatorId, planDto));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<PlanDto> updatePlan(@PathVariable Long id,
                                             @Valid @RequestBody PlanDto planDto) {

        logger.info("Updating plan id={}", id);
        return ResponseEntity.ok(operatorService.updatePlan(id, planDto));
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        logger.info("Deleting plan id={}", id);
        operatorService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}