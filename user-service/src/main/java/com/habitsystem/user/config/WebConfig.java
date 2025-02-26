package com.habitsystem.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .exposedHeaders("Authorization");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .excludePathPatterns(
                    "/api/v1/health/**",          // Health check endpoints
                    "/api/v1/public/**",          // Public endpoints
                    "/actuator/**",               // Actuator endpoints
                    "/error",                     // Error pages
                    "/swagger-ui/**",             // Swagger UI
                    "/v3/api-docs/**"            // OpenAPI docs
                )
                .addPathPatterns("/api/v1/**");   // Protect all other API endpoints
    }
}
