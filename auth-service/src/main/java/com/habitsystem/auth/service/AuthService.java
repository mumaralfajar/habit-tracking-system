package com.habitsystem.auth.service;

import com.habitsystem.auth.dto.AuthResponse;
import com.habitsystem.auth.dto.LoginRequest;
import com.habitsystem.auth.dto.RegisterRequest;
import com.habitsystem.auth.dto.UserRegistrationResponse;
import com.habitsystem.auth.event.UserRegisteredEvent;
import com.habitsystem.auth.exception.AuthenticationException;
import com.habitsystem.auth.model.AuthToken;
import com.habitsystem.auth.model.VerificationToken;
import com.habitsystem.auth.repository.AuthTokenRepository;
import com.habitsystem.auth.repository.VerificationTokenRepository;
import com.habitsystem.proto.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.KafkaException;
import org.springframework.transaction.TransactionException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserServiceGrpc.UserServiceBlockingStub userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenRepository authTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Value("${verification.token.expiration:1440}") // Added default value of 1440 minutes (24 hours)
    private long verificationTokenExpiration;

    @Transactional
    @Retryable(
        value = {KafkaException.class, TransactionException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public UserRegistrationResponse register(RegisterRequest request) {
        try {
            CreateUserRequest grpcRequest = CreateUserRequest.newBuilder()
                .setUsername(request.getUsername())
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .build();

            UserResponse userResponse = userService.createUser(grpcRequest);
            UUID userId = UUID.fromString(userResponse.getUserId());

            // Generate verification token
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(token);
            verificationToken.setUserId(userId);
            verificationToken.setExpiresAt(LocalDateTime.now().plusMinutes(verificationTokenExpiration));
            verificationTokenRepository.save(verificationToken);

            // Send verification email via Kafka with error handling and transactions
            UserRegisteredEvent event = new UserRegisteredEvent(
                userResponse.getUserId(),
                userResponse.getUsername(),
                userResponse.getEmail(),
                token
            );

            try {
                // Execute in a Kafka transaction for exactly-once semantics
                kafkaTemplate.executeInTransaction(operations -> {
                    try {
                        SendResult<String, UserRegisteredEvent> result = 
                            operations.send("user-registered", event).get(10, TimeUnit.SECONDS);
                        log.info("Verification email event sent successfully: topic={}, partition={}, offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                        return result;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new KafkaException("Kafka send interrupted", e);
                    } catch (ExecutionException e) {
                        throw new KafkaException("Error executing Kafka send", e.getCause());
                    } catch (TimeoutException e) {
                        throw new KafkaException("Timeout waiting for Kafka send to complete", e);
                    }
                });
            } catch (Exception e) {
                log.error("Failed to send verification email event: {}", e.getMessage(), e);
                // More sophisticated error handling - store failed events for retry
                saveFailedEvent(event, e);
                // Don't fail registration, continue with response
            }

            return UserRegistrationResponse.builder()
                .userId(userResponse.getUserId())
                .username(userResponse.getUsername())
                .email(userResponse.getEmail())
                .build();
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }

    // Add this method to handle failed events
    private void saveFailedEvent(UserRegisteredEvent event, Exception e) {
        try {
            // Send to a dead letter queue for later processing
            kafkaTemplate.send("user-registered-dlt", event.getUserId(), event);
            log.info("Failed event moved to DLQ: userId={}", event.getUserId());
        } catch (Exception dlqEx) {
            log.error("Failed to send to DLQ: {}", dlqEx.getMessage(), dlqEx);
        }
    }

    @Transactional
    public void verifyEmail(String token) {
        log.info("Processing email verification for token: {}", token);
        
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> {
                log.error("Invalid verification token: {}", token);
                return new AuthenticationException("Invalid or expired verification token");
            });

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.error("Expired verification token: {}", token);
            throw new AuthenticationException("Verification token has expired. Please request a new one.");
        }

        if (verificationToken.getUsedAt() != null) {
            log.warn("Attempt to reuse verification token: {}", token);
            throw new AuthenticationException("This verification link has already been used.");
        }

        UUID userId = verificationToken.getUserId();

        try {
            log.debug("Attempting to update user {} verification status via gRPC", userId);
            
            // Update user's verified status via gRPC with timeout
            VerifyEmailRequest verifyEmailRequest = VerifyEmailRequest.newBuilder()
                .setUserId(userId.toString())
                .setEmailVerified(true)
                .build();

            log.info("Verify email request: {}", verifyEmailRequest);

            UserResponse response = userService.withDeadlineAfter(5, TimeUnit.SECONDS)
                .verifyEmail(verifyEmailRequest);
                
            log.info("Successfully verified email for user: {} with response: {}", 
                userId, response.getEmailVerified());

            // Mark token as used
            verificationToken.setUsedAt(LocalDateTime.now());
            verificationTokenRepository.save(verificationToken);
            
        } catch (Exception e) {
            log.error("Failed to verify email for user {}: {}", userId, e.getMessage(), e);
            throw new AuthenticationException("Failed to verify email: " + 
                (e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.debug("Processing login request for user: {}", request.getUsername());
        
        // Get user from user service
        UserResponse user = userService.getUserByUsername(
            GetUserByUsernameRequest.newBuilder()
                .setUsername(request.getUsername())
                .build()
        );

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", request.getUsername());
            throw new AuthenticationException("Invalid credentials");
        }

        // Check if email is verified
        if (!user.getEmailVerified()) {
            log.warn("Attempt to login with unverified email: {}", request.getUsername());
            throw new AuthenticationException("Please verify your email before logging in");
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUserId(), user.getUsername());

        // Save refresh token with expiration
        AuthToken token = new AuthToken();
        token.setUserId(UUID.fromString(user.getUserId()));
        token.setRefreshToken(refreshToken);
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        authTokenRepository.save(token);

        log.info("Login successful for user: {}", request.getUsername());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600) // 1 hour
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Processing refresh token request");

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthenticationException("Refresh token is required");
        }

        // Validate JWT format and signature
        if (!jwtService.isTokenValid(refreshToken)) {
            log.warn("Invalid refresh token format or signature");
            throw new AuthenticationException("Invalid refresh token");
        }

        // Check if token exists in database
        AuthToken token = authTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found in database");
                    return new AuthenticationException("Invalid refresh token");
                });

        // Validate token status
        if (token.isBlacklisted()) {
            log.warn("Attempt to use blacklisted refresh token");
            throw new AuthenticationException("Token has been revoked");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Attempt to use expired refresh token");
            throw new AuthenticationException("Refresh token has expired");
        }

        try {
            // Get user details
            UserResponse user = userService.getUserById(
                GetUserByIdRequest.newBuilder()
                    .setUserId(token.getUserId().toString())
                    .build()
            );

            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(user.getUserId(), user.getUsername());
            String newRefreshToken = jwtService.generateRefreshToken(user.getUserId(), user.getUsername());

            // Invalidate old refresh token
            token.setBlacklisted(true);
            authTokenRepository.save(token);

            // Save new refresh token
            AuthToken newToken = new AuthToken();
            newToken.setUserId(UUID.fromString(user.getUserId()));
            newToken.setRefreshToken(newRefreshToken);
            newToken.setExpiresAt(LocalDateTime.now().plusDays(7));
            authTokenRepository.save(newToken);

            log.info("Successfully refreshed tokens for user: {}", user.getUsername());

            return AuthResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(3600)
                    .tokenType("Bearer")
                    .build();

        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage(), e);
            throw new AuthenticationException("Failed to refresh token: " + e.getMessage());
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        AuthToken token = authTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException("Refresh token not found"));

        token.setBlacklisted(true);
        authTokenRepository.save(token);
    }

    @Transactional
    public void resendVerificationEmail(String userId) {
        log.info("Resending verification email for user ID: {}", userId);
        
        // Get user details
        UserResponse user = userService.getUserById(
            GetUserByIdRequest.newBuilder()
                .setUserId(userId)  // Already String, no conversion needed
                .build()
        );

        // Generate new verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUserId(UUID.fromString(userId));  // Convert String to UUID
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        // Send new verification email with transaction support
        UserRegisteredEvent event = new UserRegisteredEvent(
            userId,
            user.getUsername(),
            user.getEmail(),
            token
        );
        
        try {
            kafkaTemplate.executeInTransaction(operations -> {
                try {
                    return operations.send("user-registered", event).get(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new KafkaException("Kafka send interrupted", e);
                } catch (ExecutionException e) {
                    throw new KafkaException("Error executing Kafka send", e.getCause());
                } catch (TimeoutException e) {
                    throw new KafkaException("Timeout waiting for Kafka send to complete", e);
                }
            });
            
            log.info("Verification email resent successfully for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to resend verification email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to resend verification email: " + e.getMessage(), e);
        }
    }
}
