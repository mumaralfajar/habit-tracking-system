package com.habitsystem.auth.service;

import com.habitsystem.proto.auth.*;
import com.habitsystem.proto.user.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.crypto.password.PasswordEncoder;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {
    
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
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
            String accessToken = jwtService.generateToken(user.getUsername(), user.getUserId());
            String refreshToken = jwtService.generateRefreshToken(user.getUserId());
            
            LoginResponse response = LoginResponse.newBuilder()
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
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        try {
            log.info("Validating token");
            boolean isValid = jwtService.validateToken(request.getToken());
            String userId = isValid ? jwtService.getUserIdFromToken(request.getToken()) : "";
            
            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                .setValid(isValid)
                .setUserId(userId)
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
