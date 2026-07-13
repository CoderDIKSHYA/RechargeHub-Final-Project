package com.capg.RechargeHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capg.RechargeHub.entity.Plan;

import java.util.List;

/*
 * Repository layer for Plan entities.
 * Provides CRUD access for plans by operator.
 */
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByOperatorId(Long operatorId);
}
