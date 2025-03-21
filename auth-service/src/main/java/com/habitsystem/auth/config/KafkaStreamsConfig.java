package com.habitsystem.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitsystem.auth.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
@Slf4j
public class KafkaStreamsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.streams.application-id:auth-service-streams}")
    private String applicationId;
    
    private static final String REGISTRATION_COUNT_STORE = "registration-count-store";
    private static final String REGISTRATION_TOPIC = "user-registered";

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        
        // Fault tolerance settings
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG), "all");
        
        // State store settings
        props.put(StreamsConfig.STATE_DIR_CONFIG, "./kafka-streams-state");
        
        return new KafkaStreamsConfiguration(props);
    }
    
    @Bean
    public StoreBuilder<KeyValueStore<String, Long>> registrationCountStore() {
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(REGISTRATION_COUNT_STORE),
                Serdes.String(),
                Serdes.Long());
    }

    @Bean
    public KStream<String, UserRegisteredEvent> kStream(StreamsBuilder streamsBuilder,
                                                        ObjectMapper objectMapper) {
        
        JsonSerde<UserRegisteredEvent> userRegisteredEventSerde = 
            new JsonSerde<>(UserRegisteredEvent.class, objectMapper);
            
        // Create stream from registration topic
        KStream<String, UserRegisteredEvent> registrationStream = 
            streamsBuilder.stream(REGISTRATION_TOPIC, 
                Consumed.with(Serdes.String(), userRegisteredEventSerde));
        
        // Process user registrations, count by domain
        KTable<String, Long> domainCounts = registrationStream
            .mapValues(event -> {
                String email = event.getEmail();
                String domain = email.substring(email.lastIndexOf('@') + 1);
                return domain;
            })
            .groupBy((key, domain) -> domain, Grouped.with(Serdes.String(), Serdes.String()))
            .count(Materialized.as(REGISTRATION_COUNT_STORE));
            
        domainCounts.toStream()
            .peek((domain, count) -> 
                log.info("Registration count for domain {}: {}", domain, count));
                
        // Additional branch for suspicious registrations (many from same domain)
        domainCounts.toStream()
            .filter((domain, count) -> count >= 10) // Threshold for monitoring
            .to("suspicious-registrations", 
                Produced.with(Serdes.String(), Serdes.Long()));
                
        return registrationStream;
    }
}
