package com.habitsystem.user.config;

import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.common.autoconfigure.GrpcCommonCodecAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    GrpcClientAutoConfiguration.class,
    GrpcCommonCodecAutoConfiguration.class
})
public class GrpcConfig {
}
