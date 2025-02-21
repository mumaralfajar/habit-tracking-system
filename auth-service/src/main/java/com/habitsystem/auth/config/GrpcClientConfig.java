package com.habitsystem.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.common.autoconfigure.GrpcCommonCodecAutoConfiguration;

@Configuration
@Import({
    GrpcClientAutoConfiguration.class,
    GrpcCommonCodecAutoConfiguration.class
})
public class GrpcClientConfig {
}
