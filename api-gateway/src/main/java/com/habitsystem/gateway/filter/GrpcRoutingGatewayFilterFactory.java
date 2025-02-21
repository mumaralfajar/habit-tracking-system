package com.habitsystem.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class GrpcRoutingGatewayFilterFactory extends AbstractGatewayFilterFactory<GrpcRoutingGatewayFilterFactory.Config> {

    public GrpcRoutingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (config.getServiceClass() == null) {
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }

            // Store gRPC service info in exchange attributes
            exchange.getAttributes().put("grpcServiceClass", config.getServiceClass());
            exchange.getAttributes().put("grpcRequestClass", config.getRequestClass());
            exchange.getAttributes().put("grpcResponseClass", config.getResponseClass());

            return chain.filter(exchange);
        };
    }

    public static class Config {
        private String serviceClass;
        private String requestClass;
        private String responseClass;

        public String getServiceClass() { return serviceClass; }
        public void setServiceClass(String serviceClass) { this.serviceClass = serviceClass; }
        public String getRequestClass() { return requestClass; }
        public void setRequestClass(String requestClass) { this.requestClass = requestClass; }
        public String getResponseClass() { return responseClass; }
        public void setResponseClass(String responseClass) { this.responseClass = responseClass; }
    }
}
