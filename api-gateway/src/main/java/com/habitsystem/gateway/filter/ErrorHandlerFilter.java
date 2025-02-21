package com.habitsystem.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitsystem.gateway.model.ErrorResponse;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ErrorHandlerFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    public ErrorHandlerFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
            .onErrorResume(throwable -> {
                ServerHttpResponse response = exchange.getResponse();
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

                ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    throwable.getMessage()
                );

                try {
                    byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
                    DataBuffer buffer = response.bufferFactory().wrap(bytes);
                    return response.writeWith(Mono.just(buffer));
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
    }

    @Override
    public int getOrder() {
        return -1; // Execute this filter last
    }
}
