package com.habitsystem.auth.controller;

import com.habitsystem.auth.dto.*;
import com.habitsystem.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegistrationResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
            
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(errorMessage));
        }

        try {
            log.info("Processing registration request for user: {}", request.getUsername());
            UserRegistrationResponse response = authService.register(request);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Registration successful. Please check your email to verify your account.",
                response
            ));
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ApiResponse.success("Login successful", response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerification(@RequestParam String userId) {
        try {
            authService.resendVerificationEmail(userId);
            return ApiResponse.<Void>success(
                "Verification email sent successfully. Please check your inbox.", 
                null
            );
        } catch (Exception e) {
            log.error("Failed to resend verification email: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ApiResponse<Void> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            return ApiResponse.<Void>success(
                "Email verified successfully. You can now log in to your account.", 
                null
            );
        } catch (Exception e) {
            log.error("Email verification failed: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            log.debug("Processing refresh token request");
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            return ApiResponse.success("Token refreshed successfully", response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestParam String refreshToken) {
        try {
            authService.logout(refreshToken);
            return ApiResponse.<Void>success("Logged out successfully", null);
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}

record ErrorResponse(String error, String message) {}
