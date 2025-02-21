package com.habitsystem.gateway.transformer;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.springframework.http.HttpMethod;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class RestGrpcTransformer {
    
    public static String transformHttpMethod(HttpMethod method) {
        if (method == HttpMethod.GET) return "get";
        if (method == HttpMethod.POST) return "create";
        if (method == HttpMethod.PUT) return "update";
        if (method == HttpMethod.DELETE) return "delete";
        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }

    public static <T extends Message.Builder> Mono<T> parseRequestBody(
            Mono<DataBuffer> body,
            T builder) {
        return body.map(buffer -> {
            try {
                String json = buffer.toString(StandardCharsets.UTF_8);
                JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
                return builder;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse request body", e);
            }
        });
    }

    public static String toJson(Message message) {
        try {
            return JsonFormat.printer()
                    .includingDefaultValueFields()
                    .print(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}
