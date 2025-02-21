package com.habitsystem.auth.controller;

import com.habitsystem.auth.dto.ApiResponse;
import com.habitsystem.auth.dto.RegisterRequest;
import com.habitsystem.auth.dto.UserRegistrationResponse;
import com.habitsystem.proto.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @PostMapping("/register")
    public ApiResponse<UserRegistrationResponse> register(@RequestBody RegisterRequest request) {
        try {
            log.debug("Received registration request: {}", request);

            CreateUserRequest grpcRequest = CreateUserRequest.newBuilder()
                .setUsername(request.getUsername())
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .build();
            
            log.debug("Sending gRPC request to user-service: {}", grpcRequest);
            UserResponse response = userService.createUser(grpcRequest);
            log.debug("Received gRPC response from user-service: {}", response);

            UserRegistrationResponse registrationResponse = UserRegistrationResponse.builder()
                .userId(response.getUserId())
                .username(response.getUsername())
                .email(response.getEmail())
                .build();

            return ApiResponse.success(
                String.format("User %s registered successfully", response.getUsername()),
                registrationResponse
            );
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}

record ErrorResponse(String error, String message) {}
