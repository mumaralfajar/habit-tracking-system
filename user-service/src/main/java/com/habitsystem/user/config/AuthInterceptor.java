package com.habitsystem.user.config;

import com.habitsystem.user.exception.UnauthorizedException;
import com.habitsystem.proto.auth.AuthServiceGrpc;
import com.habitsystem.proto.auth.ValidateAuthRequest;
import com.habitsystem.proto.auth.ValidateAuthResponse;

import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    public AuthInterceptor(@GrpcClient("auth-service") Channel channel) {
        this.authServiceStub = AuthServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        String requestPath = request.getRequestURI();
        log.debug("Processing request for path: {}", requestPath);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        validateAuthHeader(authHeader);

        String token = extractToken(authHeader);
        validateToken(token);

        // Store authenticated user info in request attributes
        var validation = validateTokenWithAuthService(token);
        storeUserInfo(request, validation);

        log.debug("Authentication successful for user: {}", validation.getUsername());
        return true;
    }

    private void validateAuthHeader(String authHeader) {
        if (authHeader == null) {
            log.warn("Missing Authorization header");
            throw new UnauthorizedException("Authorization header is required");
        }
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Invalid Authorization header format");
            throw new UnauthorizedException("Bearer token is required");
        }
    }

    private String extractToken(String authHeader) {
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            log.warn("Empty token provided");
            throw new UnauthorizedException("Token cannot be empty");
        }
        return token;
    }

    private void validateToken(String token) {
        if (token.length() < 10) { // Basic length validation
            log.warn("Token too short to be valid");
            throw new UnauthorizedException("Invalid token format");
        }
    }

    private ValidateAuthResponse validateTokenWithAuthService(String token) {
        try {
            var validateRequest = ValidateAuthRequest.newBuilder()
                    .setToken(token)
                    .build();
                    
            var validation = authServiceStub.validateAuth(validateRequest);
            
            if (!validation.getValid()) {
                log.warn("Token validation failed");
                throw new UnauthorizedException("Invalid or expired token");
            }
            
            return validation;
        } catch (StatusRuntimeException e) {
            log.error("Error validating token with auth service: {}", e.getMessage());
            throw new UnauthorizedException("Authentication service unavailable");
        }
    }

    private void storeUserInfo(HttpServletRequest request, ValidateAuthResponse validation) {
        request.setAttribute("userId", validation.getUserId());
        request.setAttribute("username", validation.getUsername());
    }
}
