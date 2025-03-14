package com.habitsystem.user.service;

import com.habitsystem.user.mapper.UserMapper;
import com.habitsystem.user.model.User;
import com.habitsystem.user.repository.UserRepository;
import com.habitsystem.user.exception.GrpcException;
import com.habitsystem.proto.user.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public void getUserByUsername(GetUserByUsernameRequest request, 
                                StreamObserver<UserResponse> responseObserver) {
        try {
            log.info("Fetching user by username: {}", request.getUsername());
            
            if (request.getUsername().isEmpty()) {
                throw new GrpcException(Status.INVALID_ARGUMENT, "Username cannot be empty");
            }

            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new GrpcException(Status.NOT_FOUND, 
                    "User not found with username: " + request.getUsername()));

            UserResponse response = mapToUserResponse(user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("Successfully fetched user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error fetching user: {}", e.getMessage(), e);
            responseObserver.onError(Status.fromThrowable(e)
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void createUser(CreateUserRequest request, 
                         StreamObserver<UserResponse> responseObserver) {
        try {
            log.info("Creating new user with username: {}", request.getUsername());
            
            validateCreateUserRequest(request);
            
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new GrpcException(Status.ALREADY_EXISTS, 
                    "Username already exists: " + request.getUsername());
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new GrpcException(Status.ALREADY_EXISTS, 
                    "Email already exists: " + request.getEmail());
            }

            // Create user with encoded password
            User user = userMapper.toEntity(request);
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            log.debug("Password encoded successfully for user: {}", request.getUsername());
            user.setPassword(encodedPassword);
            user.setEmailVerified(false);
            
            User savedUser = userRepository.save(user);
            
            UserResponse response = mapToUserResponse(savedUser);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("Successfully created user: {}", savedUser.getUsername());
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            responseObserver.onError(Status.fromThrowable(e)
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void getUserById(GetUserByIdRequest request, 
                          StreamObserver<UserResponse> responseObserver) {
        try {
            log.info("Fetching user by ID: {}", request.getUserId());
            
            if (request.getUserId().isEmpty()) {
                throw new GrpcException(Status.INVALID_ARGUMENT, "User ID cannot be empty");
            }

            User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new GrpcException(Status.NOT_FOUND, 
                    "User not found with ID: " + request.getUserId()));

            UserResponse response = mapToUserResponse(user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("Successfully fetched user with ID: {}", user.getId());
        } catch (Exception e) {
            log.error("Error fetching user by ID: {}", e.getMessage(), e);
            responseObserver.onError(Status.fromThrowable(e)
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            log.info("Updating user with ID: {}", request.getUserId());
            
            if (request.getUserId().isEmpty()) {
                throw new GrpcException(Status.INVALID_ARGUMENT, "User ID cannot be empty");
            }

            User user = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new GrpcException(Status.NOT_FOUND, 
                    "User not found with ID: " + request.getUserId()));

            log.debug("Found user: {}. Current email verified status: {}", 
                user.getId(), user.getEmailVerified());

            // Update email verified status if present in request
            if (request.getEmailVerified() != user.getEmailVerified()) {
                user.setEmailVerified(request.getEmailVerified());
                log.debug("Updating email verified status to: {}", request.getEmailVerified());
            }

            User updatedUser = userRepository.save(user);
            log.info("Successfully updated user: {}. New email verified status: {}", 
                updatedUser.getId(), updatedUser.getEmailVerified());

            UserResponse response = userMapper.toProto(updatedUser);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            String errorMessage = "Error updating user: " + e.getMessage();
            log.error(errorMessage, e);
            Status status = (e instanceof GrpcException) ? 
                ((GrpcException) e).getStatus() : Status.INTERNAL;
            responseObserver.onError(status
                .withDescription(errorMessage)
                .asRuntimeException());
        }
    }

    private void validateCreateUserRequest(CreateUserRequest request) {
        if (request.getUsername().isEmpty()) {
            throw new GrpcException(Status.INVALID_ARGUMENT, "Username cannot be empty");
        }
        if (request.getEmail().isEmpty()) {
            throw new GrpcException(Status.INVALID_ARGUMENT, "Email cannot be empty");
        }
        if (request.getPassword().isEmpty()) {
            throw new GrpcException(Status.INVALID_ARGUMENT, "Password cannot be empty");
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return userMapper.toProto(user);
    }
}
