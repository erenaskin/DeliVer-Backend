package com.deliverapp.backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    
    // Backward compatibility için
    public AuthResponse(String token) {
        this.token = token;
        this.tokenType = "Bearer";
    }
}

