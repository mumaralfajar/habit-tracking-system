package com.habitsystem.auth.config;

import com.habitsystem.proto.user.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {
    
    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceStub(
            @GrpcClient("user-service") UserServiceGrpc.UserServiceBlockingStub stub) {
        return stub;
    }
}
