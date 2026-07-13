/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : UserRepository
 * DESCRIPTION:
 *   Spring Data JPA repository for the User entity.
 *   Provides standard CRUD operations and a custom query method
 *   to find users by email address for authentication.
 * ================================================================ */
package com.capg.RechargeHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capg.RechargeHub.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
