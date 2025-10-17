package com.deliverapp.backend.controller;

import com.deliverapp.backend.dto.request.LoginRequest;
import com.deliverapp.backend.dto.request.EmailVerificationRequest;
import com.deliverapp.backend.dto.request.PasswordResetRequest;
import com.deliverapp.backend.dto.request.RefreshTokenRequest;
import com.deliverapp.backend.dto.request.RegisterRequest;
import com.deliverapp.backend.dto.response.AuthResponse;
import com.deliverapp.backend.service.AuthService;
import com.deliverapp.backend.service.EmailService;
import com.deliverapp.backend.service.VerificationCodeService;
import com.deliverapp.backend.service.AuditLogService;
import com.deliverapp.backend.model.User;
import com.deliverapp.backend.model.VerificationCode;
import com.deliverapp.backend.repository.UserRepository;
import com.deliverapp.backend.security.TokenBlacklist;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;
    private final UserRepository userRepository;
    private final TokenBlacklist tokenBlacklist;
    private final AuditLogService auditLogService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/verify-email/send")
    public ResponseEntity<String> sendVerificationCode(@Valid @RequestBody EmailVerificationRequest request) {
    VerificationCode vc = verificationCodeService.createCode(request.getEmail(), "EMAIL_VERIFICATION");
    emailService.sendVerificationEmail(request.getEmail(), vc.getCode());
    return ResponseEntity.ok("Doğrulama kodu gönderildi: " + vc.getCode());
    }

    @PostMapping("/verify-email/confirm")
    public ResponseEntity<String> confirmEmail(@RequestParam String code) {
        try {
            // Code'dan verification code'u bul
            Optional<VerificationCode> verificationCodeOpt = verificationCodeService.findByCode(code);

            if (verificationCodeOpt.isPresent()) {
                VerificationCode verificationCode = verificationCodeOpt.get();
                String email = verificationCode.getEmail();

                // Süre ve kullanım kontrolü
                if (verificationCode.getExpiresAt().isAfter(LocalDateTime.now()) && !verificationCode.isUsed()) {

                    // Kodu kullanıldı olarak işaretle
                    verificationCode.setUsed(true);
                    verificationCodeService.save(verificationCode); // veya update metodu

                    // User'ı bul ve email verified yap
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user != null) {
                        user.setEmailVerified(true);
                        userRepository.save(user);
                        auditLogService.log("EMAIL_VERIFIED", user.getEmail(), "Kullanıcı e-posta doğruladı.");
                        return ResponseEntity.ok("E-posta doğrulandı.");
                    } else {
                        return ResponseEntity.badRequest().body("Kullanıcı bulunamadı.");
                    }
                } else {
                    return ResponseEntity.badRequest().body("Kod süresi dolmuş veya kullanılmış.");
                }
            }
            return ResponseEntity.badRequest().body("Geçersiz kod.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password/send")
    public ResponseEntity<String> sendPasswordResetCode(@Valid @RequestBody PasswordResetRequest request) {
    VerificationCode vc = verificationCodeService.createCode(request.getEmail(), "PASSWORD_RESET");
    emailService.sendPasswordResetEmail(request.getEmail(), vc.getCode());
    return ResponseEntity.ok("Şifre sıfırlama kodu gönderildi: " + vc.getCode());
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> confirmPasswordReset(@Valid @RequestBody PasswordResetRequest request, @RequestParam String code, @RequestParam String newPassword) {
        boolean verified = verificationCodeService.verifyCode(request.getEmail(), code);
        if (verified) {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null) {
                String hashedPassword = authService.hashPassword(newPassword);
                user.setPassword(hashedPassword);
                userRepository.save(user);
                // Audit log: password reset
                auditLogService.log("PASSWORD_RESET", user.getEmail(), "Kullanıcı şifresini sıfırladı.");
            }
            return ResponseEntity.ok("Şifre başarıyla güncellendi.");
        }
        return ResponseEntity.badRequest().body("Kod geçersiz veya süresi dolmuş.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklist.blacklistToken(token);
            return ResponseEntity.ok("Çıkış yapıldı, token iptal edildi.");
        } else {
            return ResponseEntity.badRequest().body("Geçersiz veya eksik Authorization header.");
        }
    }
}
