package com.capg.RechargeHub.repository;

import com.capg.RechargeHub.entity.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp, Long> {
    Optional<UserOtp> findByEmail(String email);
    void deleteByEmail(String email);
}
