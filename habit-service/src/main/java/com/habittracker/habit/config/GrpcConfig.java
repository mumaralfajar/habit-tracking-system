package com.habittracker.habit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import io.grpc.ServerInterceptor;

@Configuration
public class GrpcConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    @GrpcGlobalServerInterceptor
    public ServerInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }
    
    @Bean
    public GlobalServerInterceptorConfigurer globalInterceptorConfigurerAdapter() {
        return registry -> registry.add(loggingInterceptor());
    }
    
    // Inner class for logging gRPC calls
    private static class LoggingInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> io.grpc.ServerCall.Listener<ReqT> interceptCall(
                io.grpc.ServerCall<ReqT, RespT> call, 
                io.grpc.Metadata headers,
                io.grpc.ServerCallHandler<ReqT, RespT> next) {
            System.out.println("gRPC call: " + call.getMethodDescriptor().getFullMethodName());
            return next.startCall(call, headers);
        }
    }
}
