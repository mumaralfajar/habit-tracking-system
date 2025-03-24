package com.habittracker.habit.filter;

import com.habittracker.habit.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        log.debug("Processing request to {}", request.getRequestURI());

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(jwt)) {
                String userId = jwtService.getUserIdFromToken(jwt);
                String username = jwtService.getUsernameFromToken(jwt);
                List<String> roles = jwtService.getRolesFromToken(jwt);
                
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (roles != null) {
                    authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                }
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
                );
                
                // Store the userId in the authentication details for later use in services
                request.setAttribute("userId", userId);
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.debug("Authenticated user: {}, with roles: {}", username, roles);
            }
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
