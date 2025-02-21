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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    
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

            User user = userMapper.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            
            User savedUser = userRepository.save(user);
            
            UserResponse response = UserResponse.newBuilder()
                .setUserId(UUID.randomUUID().toString())
                .setUsername(savedUser.getUsername())
                .setEmail(savedUser.getEmail())
                .build();

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
