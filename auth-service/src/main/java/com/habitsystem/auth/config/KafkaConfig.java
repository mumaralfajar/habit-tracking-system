package com.habitsystem.auth.config;

import com.habitsystem.auth.event.UserRegisteredEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.kafka.support.ProducerListener;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.producer.transaction-id-prefix:auth-tx-}")
    private String transactionIdPrefix;

    @Bean
    public ProducerFactory<String, UserRegisteredEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Enable idempotence for exactly once delivery semantics
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Set acks to 'all' for strongest durability guarantee
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        
        // Increase retries
        configProps.put(ProducerConfig.RETRIES_CONFIG, 10);
        
        // Using string for min.insync.replicas as it's a broker config, not a producer config
        configProps.put("min.insync.replicas", 2);
        
        // Set linger to batch more messages (improves throughput)
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        
        // Configure transaction support
        configProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionIdPrefix);
        
        // Set delivery timeout
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate() {
        KafkaTemplate<String, UserRegisteredEvent> template = new KafkaTemplate<>(producerFactory());
        template.setProducerListener(producerListener());
        return template;
    }
    
    @Bean(name = "transactionManager")
    public KafkaTransactionManager<String, UserRegisteredEvent> kafkaTransactionManager() {
        return new KafkaTransactionManager<>(producerFactory());
    }
    
    @Bean
    public ProducerListener<String, UserRegisteredEvent> producerListener() {
        return new ProducerListener<>() {
            @Override
            public void onSuccess(ProducerRecord<String, UserRegisteredEvent> producerRecord, RecordMetadata recordMetadata) {
                log.info("Successfully sent message to topic {} with key {} at partition {} offset {}",
                         producerRecord.topic(), producerRecord.key(), recordMetadata.partition(), recordMetadata.offset());
            }

            @Override
            public void onError(ProducerRecord<String, UserRegisteredEvent> producerRecord, RecordMetadata recordMetadata, Exception exception) {
                log.error("Failed to send message to topic {} with key {}: {}",
                          producerRecord.topic(), producerRecord.key(), exception.getMessage(), exception);
            }
        };
    }
}
