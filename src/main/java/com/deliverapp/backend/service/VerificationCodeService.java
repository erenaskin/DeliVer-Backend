package com.deliverapp.backend.service;

import com.deliverapp.backend.model.VerificationCode;
import com.deliverapp.backend.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {
    private final VerificationCodeRepository verificationCodeRepository;

    public VerificationCode createCode(String email) {
        return createCode(email, "EMAIL_VERIFICATION");
        }
    public Optional<VerificationCode> findByCode(String code) {
        return verificationCodeRepository.findByCode(code);
    }

    public VerificationCode save(VerificationCode verificationCode) {
        return verificationCodeRepository.save(verificationCode);
    }
        public VerificationCode createCode(String email, String usageType) {
        String code = String.valueOf((int)(Math.random()*900000)+100000);
        VerificationCode verificationCode = VerificationCode.builder()
            .email(email)
            .code(code)
            .expiresAt(LocalDateTime.now().plusMinutes(10))
            .used(false)
            .usageType(usageType)
            .build();
        return verificationCodeRepository.save(verificationCode);
    }

    public boolean verifyCode(String email, String code) {
        Optional<VerificationCode> optional = verificationCodeRepository.findByEmailAndCodeAndUsedFalse(email, code);
        if (optional.isPresent() && optional.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            VerificationCode vc = optional.get();
            vc.setUsed(true);
            verificationCodeRepository.save(vc);
            return true;
        }
        return false;
    }
}
