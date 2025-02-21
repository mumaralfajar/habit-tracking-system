package com.habitsystem.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegistrationResponse {
    private String userId;
    private String username;
    private String email;
}