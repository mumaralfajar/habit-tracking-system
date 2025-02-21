package com.habitsystem.user.health;

import net.devh.boot.grpc.server.serverfactory.GrpcServerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import io.grpc.Server;

@Component
public class GrpcHealthIndicator implements HealthIndicator {
    
    private final GrpcServerFactory grpcServerFactory;
    
    public GrpcHealthIndicator(GrpcServerFactory grpcServerFactory) {
        this.grpcServerFactory = grpcServerFactory;
    }

    @Override
    public Health health() {
        try {
            if (grpcServerFactory != null) {
                Server server = grpcServerFactory.createServer();
                if (server != null && !server.isShutdown()) {
                    return Health.up()
                        .withDetail("grpcServer", "running")
                        .withDetail("port", grpcServerFactory.getPort())
                        .build();
                }
            }
            return Health.down()
                .withDetail("grpcServer", "not running")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("grpcServer", "error")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
