package com.habitsystem.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RequestTransformationGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestTransformationGatewayFilterFactory.Config> {

    public RequestTransformationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Correlation-ID", java.util.UUID.randomUUID().toString())
                .build();
            
            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}
