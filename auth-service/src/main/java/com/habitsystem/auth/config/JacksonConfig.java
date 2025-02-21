package com.habitsystem.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.protobuf.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule protoModule = new SimpleModule();
        protoModule.addSerializer(Message.class, new ProtobufSerializer());
        mapper.registerModule(protoModule);
        return mapper;
    }
}