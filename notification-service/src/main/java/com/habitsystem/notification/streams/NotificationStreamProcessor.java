package com.habitsystem.notification.streams;

import com.habitsystem.notification.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.WindowedSerdes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class NotificationStreamProcessor {

    private static final String EMAIL_DOMAIN_COUNTS_STORE = "email-domain-counts";
    
    @Autowired
    public void buildNotificationPipeline(StreamsBuilder streamsBuilder) {
        // Create JsonSerde for UserRegisteredEvent
        final JsonSerde<UserRegisteredEvent> userSerde = new JsonSerde<>(UserRegisteredEvent.class);
        
        // Create a stream from the user-registered topic
        KStream<String, UserRegisteredEvent> userStream = streamsBuilder
            .stream("user-registered", Consumed.with(Serdes.String(), userSerde));
        
        // Log message metadata
        userStream.peek((key, value) -> 
            log.info("Processing notification stream event: user={}, email={}",
                    value.getUsername(), value.getEmail()));
        
        // Count registrations by email domain in 1-hour tumbling windows
        userStream
            .groupBy((key, value) -> extractDomain(value.getEmail()), 
                    Grouped.with(Serdes.String(), userSerde))
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1)))
            .count(Materialized.as(EMAIL_DOMAIN_COUNTS_STORE))
            .toStream()
            .peek((windowedKey, count) -> 
                log.info("Domain stats: domain={}, window={}, count={}", 
                        windowedKey.key(), windowedKey.window().startTime(), count))
            .filter((windowedKey, count) -> count > 10) // Threshold for monitoring
            .mapValues((windowedKey, count) -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("domain", windowedKey.key());
                alert.put("count", count);
                alert.put("windowStart", windowedKey.window().startTime());
                alert.put("windowEnd", windowedKey.window().endTime());
                return alert;
            })
            .to("notification-alerts", Produced.with(
                    // Replace deprecated method with current approach
                    WindowedSerdes.timeWindowedSerdeFrom(String.class, Duration.ofHours(1).toMillis()),
                    new JsonSerde<>(Map.class)));
                    
        // Additional stream for processing failures
        streamsBuilder
            .stream("user-registered-dlq", Consumed.with(Serdes.String(), userSerde))
            .peek((key, value) -> 
                log.warn("Processing DLQ event in stream: userId={}", value.getUserId()))
            .groupBy((key, value) -> value.getUserId())
            .count()
            .toStream()
            .filter((userId, count) -> count >= 3) // Alert on multiple failures
            .to("notification-failures", Produced.with(Serdes.String(), Serdes.Long()));
    }
    
    private String extractDomain(String email) {
        return email.substring(email.lastIndexOf('@') + 1);
    }
}
