package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByEmailAndCodeAndUsedFalse(String email, String code);
    Optional<VerificationCode> findByCode(String code);
    Optional<VerificationCode> findByEmailAndUsageType(String email, String usageType);
}
