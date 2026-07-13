package com.capg.RechargeHub.controller;

import com.capg.RechargeHub.service.UserService;
import com.capg.RechargeHub.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicStatsController {

    private final UserRepository userRepository;

    public PublicStatsController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get Public Stats", description = "Fetch total users for landing page")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPublicStats() {
        Map<String, Object> stats = new HashMap<>();
        long userCount = userRepository.count();
        
        stats.put("totalUsers", userCount + 500); // 500 base + actual users
        stats.put("totalSavings", (userCount * 120) + 8000); // Dynamic savings
        stats.put("successRate", "99.9%");
        
        return ResponseEntity.ok(stats);
    }
}
