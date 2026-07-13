package com.capg.RechargeHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capg.RechargeHub.entity.Recharge;

import java.util.List;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : RechargeRepository
 * DESCRIPTION:
 *   Spring Data JPA repository for the Recharge entity.
 *   Provides standard CRUD operations and custom query methods
 *   for fetching recharges by user ID with optional ordering.
 * ================================================================ */
@Repository
public interface RechargeRepository extends JpaRepository<Recharge, Long> {

    /* ================================================================
     * METHOD: findByUserId
     * DESCRIPTION:
     *   Returns all recharge records associated with the given user ID.
     * ================================================================ */
    List<Recharge> findByUserId(Long userId);

    /* ================================================================
     * METHOD: findByUserIdOrderByCreatedAtDesc
     * DESCRIPTION:
     *   Returns recharge records for a user sorted by creation date
     *   in descending order (most recent first).
     * ================================================================ */
    List<Recharge> findByUserIdOrderByCreatedAtDesc(Long userId);
}
