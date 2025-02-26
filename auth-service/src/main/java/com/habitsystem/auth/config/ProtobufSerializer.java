package com.habitsystem.auth.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;

public class ProtobufSerializer extends JsonSerializer<Message> {
    private static final JsonFormat.Printer PRINTER = JsonFormat.printer()
        .omittingInsignificantWhitespace()
        .preservingProtoFieldNames();

    @Override
    public void serialize(Message value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String json = PRINTER.print(value);
        gen.writeRawValue(json);
    }
}