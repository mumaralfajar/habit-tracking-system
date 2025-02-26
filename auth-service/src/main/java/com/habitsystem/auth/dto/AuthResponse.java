package com.habitsystem.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String userId;
    private String username;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;
}
