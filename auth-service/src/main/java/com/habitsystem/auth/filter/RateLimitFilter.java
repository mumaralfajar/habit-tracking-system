package com.habitsystem.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.habitsystem.auth.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, AtomicInteger> requestCounts;
    private final ObjectMapper objectMapper;
    private final int maxRequests;
    private final int windowMinutes;

    public RateLimitFilter(
            ObjectMapper objectMapper,
            @Value("${rate.limit.max-requests:60}") int maxRequests,
            @Value("${rate.limit.window-minutes:1}") int windowMinutes) {
        this.objectMapper = objectMapper;
        this.maxRequests = maxRequests;
        this.windowMinutes = windowMinutes;
        this.requestCounts = Caffeine.newBuilder()
                .expireAfterWrite(windowMinutes, TimeUnit.MINUTES)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        AtomicInteger counter = requestCounts.get(clientIp, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();

        log.debug("Rate limit for IP {}: {}/{} requests", clientIp, currentCount, maxRequests);

        if (currentCount > maxRequests) {
            log.warn("Rate limit exceeded for IP: {} ({}/{})", clientIp, currentCount, maxRequests);
            sendRateLimitResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        ApiResponse<?> apiResponse = ApiResponse.error(
            String.format("Rate limit exceeded. Please try again after %d minute(s)", windowMinutes)
        );
        
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/actuator/health") || 
               path.startsWith("/actuator/") || 
               path.equals("/error") ||
               path.equals("/") ||
               path.equals("/favicon.ico");
    }
}
