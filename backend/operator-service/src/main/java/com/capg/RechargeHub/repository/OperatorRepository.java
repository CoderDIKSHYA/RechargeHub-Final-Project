package com.capg.RechargeHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capg.RechargeHub.entity.Operator;

/*
 * Repository layer for Operator entities.
 * Provides CRUD access for operators.
 */
public interface OperatorRepository extends JpaRepository<Operator, Long> {
}
