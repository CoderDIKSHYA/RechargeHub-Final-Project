/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : TransactionRepository
 * DESCRIPTION:
 *   Spring Data JPA repository for the Transaction entity.
 *   Provides standard CRUD operations and custom query methods
 *   to fetch transactions by user ID or recharge ID.
 * ================================================================ */
package com.capg.RechargeHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capg.RechargeHub.entity.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByRechargeId(Long rechargeId);
}
