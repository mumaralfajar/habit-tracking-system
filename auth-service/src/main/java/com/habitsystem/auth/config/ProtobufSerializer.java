package com.habitsystem.auth.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;  // Add this import
import java.io.IOException;

public class ProtobufSerializer extends JsonSerializer<Message> {
    @Override
    public void serialize(Message value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String json = JsonFormat.printer()
            .includingDefaultValueFields()
            .print(value);
        gen.writeRawValue(json);
    }
}