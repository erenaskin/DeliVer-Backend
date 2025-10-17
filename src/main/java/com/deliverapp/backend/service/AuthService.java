package com.deliverapp.backend.service;


import com.deliverapp.backend.dto.request.LoginRequest;
import com.deliverapp.backend.dto.request.RegisterRequest;
import com.deliverapp.backend.dto.response.AuthResponse;
import com.deliverapp.backend.model.Role;
import com.deliverapp.backend.model.User;
import com.deliverapp.backend.repository.UserRepository;
import com.deliverapp.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Transactional
    public void deleteUser(Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
            userRepository.delete(user);
            auditLogService.log("DELETE", user.getUsername(), "Kullanıcı hesabı silindi");
        } catch (Exception e) {
            auditLogService.log("DELETE-FAIL", String.valueOf(userId), "Kullanıcı silme başarısız: " + e.getMessage());
            throw e;
        }
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    @Transactional
    public String register(RegisterRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            auditLogService.log("REGISTER-FAIL", "", "Email alanı boş");
            throw new IllegalArgumentException("Email alanı zorunludur");
        }
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            auditLogService.log("REGISTER-FAIL", request.getEmail(), "Username alanı boş");
            throw new IllegalArgumentException("Username alanı zorunludur");
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            auditLogService.log("REGISTER-FAIL", request.getEmail(), "Şifre çok kısa");
            throw new IllegalArgumentException("Şifre en az 6 karakter olmalıdır");
        }
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            auditLogService.log("REGISTER-FAIL", request.getEmail(), "Email zaten kayıtlı");
            throw new IllegalArgumentException("Bu email adresi zaten kayıtlı");
        }
        
        if (userRepository.existsByUsername(request.getUsername())) {
            auditLogService.log("REGISTER-FAIL", request.getUsername(), "Username zaten kayıtlı");
            throw new IllegalArgumentException("Bu kullanıcı adı zaten alınmış");
        }

        User user = User.builder()
            .username(request.getUsername().trim())
            .email(request.getEmail().trim().toLowerCase())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .emailVerified(false) // Email verification required
            .build();

        userRepository.save(user);
        auditLogService.log("REGISTER", user.getUsername(), "Kullanıcı kaydı oluşturuldu");

        return jwtTokenProvider.generateToken(user);
    }
    
    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            auditLogService.log("LOGIN-FAIL", "", "Email alanı boş!");
            throw new IllegalArgumentException("Email alanı boş!");
        }
        
        // Check if user exists before authentication attempt
        if (!userRepository.existsByEmail(request.getEmail())) {
            auditLogService.log("LOGIN-FAIL", request.getEmail(), "Kullanıcı bulunamadı");
            throw new IllegalArgumentException("Geçersiz email veya şifre");
        }
        
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            User authenticatedUser = (User) auth.getPrincipal();
            auditLogService.log("LOGIN", authenticatedUser.getUsername(), "Kullanıcı giriş yaptı");
            
            String accessToken = jwtTokenProvider.generateToken(authenticatedUser);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authenticatedUser);
            
            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(86400L) // 24 saat (saniye cinsinden)
                    .build();
        } catch (Exception e) {
            auditLogService.log("LOGIN-FAIL", request.getEmail(), "Giriş başarısız: " + e.getMessage());
            throw new IllegalArgumentException("Geçersiz email veya şifre");
        }
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        try {
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                throw new IllegalArgumentException("Geçersiz refresh token");
            }
            
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
            
            String newAccessToken = jwtTokenProvider.generateToken(user);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
            
            auditLogService.log("TOKEN_REFRESH", user.getUsername(), "Token yenilendi");
            
            return AuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(86400L) // 24 saat
                    .build();
                    
        } catch (Exception e) {
            auditLogService.log("TOKEN_REFRESH_FAIL", "", "Token yenileme başarısız: " + e.getMessage());
            throw new IllegalArgumentException("Token yenileme başarısız");
        }
    }
}

