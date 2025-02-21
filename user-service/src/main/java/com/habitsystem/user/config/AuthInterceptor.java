package com.habitsystem.user.config;

import com.habitsystem.user.exception.UnauthorizedException;
import com.habitsystem.proto.auth.AuthServiceGrpc;
import com.habitsystem.proto.auth.ValidateTokenRequest;
import io.grpc.Channel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

    @Autowired
    public AuthInterceptor(
            @GrpcClient("auth-service") Channel channel) {
        this.authServiceStub = AuthServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        if (token.isEmpty()) {
            throw new UnauthorizedException("Empty token");
        }

        var validateRequest = ValidateTokenRequest.newBuilder()
                .setToken(token)
                .build();
                
        var validation = authServiceStub.validateToken(validateRequest);
        
        if (!validation.getValid()) {
            throw new UnauthorizedException("Invalid token");
        }
        
        // Add userId to request attributes
        request.setAttribute("userId", validation.getUserId());
        return true;
    }
}
