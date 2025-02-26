package com.habitsystem.auth.service;

import com.habitsystem.proto.auth.*;
import com.habitsystem.proto.user.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.crypto.password.PasswordEncoder;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {
    
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @Override
    public void authenticate(AuthRequest request, StreamObserver<AuthResponse> responseObserver) {
        try {
            log.info("Processing login request for user: {}", request.getUsername());
            
            // Call User Service to verify credentials
            GetUserByUsernameRequest userRequest = GetUserByUsernameRequest.newBuilder()
                .setUsername(request.getUsername())
                .build();
            
            UserResponse user;
            try {
                user = userService.getUserByUsername(userRequest);
            } catch (StatusRuntimeException e) {
                log.error("User service error: {}", e.getMessage());
                responseObserver.onError(e);
                return;
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw Status.UNAUTHENTICATED.withDescription("Invalid credentials").asRuntimeException();
            }

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user.getUserId(), user.getUsername());
            String refreshToken = jwtService.generateRefreshToken(user.getUserId(), user.getUsername());
            
            AuthResponse response = AuthResponse.newBuilder()
                .setToken(accessToken)
                .setRefreshToken(refreshToken)
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Login successful for user: {}", request.getUsername());
            
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Authentication failed")
                .withCause(e)
                .asRuntimeException());
        }
    }

    @Override
    public void validateAuth(ValidateAuthRequest request, StreamObserver<ValidateAuthResponse> responseObserver) {
        try {
            log.info("Validating token");
            boolean isValid = jwtService.isTokenValid(request.getToken());
            String userId = isValid ? jwtService.getUserIdFromToken(request.getToken()) : "";
            String username = isValid ? jwtService.getUsernameFromToken(request.getToken()) : "";
            
            ValidateAuthResponse response = ValidateAuthResponse.newBuilder()
                .setValid(isValid)
                .setUserId(userId)
                .setUsername(username)
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Token validation completed. Valid: {}", isValid);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Token validation failed")
                .withCause(e)
                .asRuntimeException());
        }
    }
}
