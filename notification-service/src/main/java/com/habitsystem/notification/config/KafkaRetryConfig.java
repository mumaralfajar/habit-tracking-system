package com.habitsystem.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(name = "spring.kafka.retry.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class KafkaRetryConfig {

    @Bean
    @Primary
    public DefaultErrorHandler errorHandler() {
        // Keep your existing error handler config
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            (record, exception) -> {
                log.error("Processing failed after retries: {}", exception.getMessage(), exception);
            }, 
            new FixedBackOff(1000L, 3)
        );
        
        // Additional configuration
        // Don't retry these exceptions
        errorHandler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            NullPointerException.class
        );
        
        return errorHandler;
    }

    // This bean is necessary to disable the RetryTopicConfiguration
    @Bean
    public TaskExecutor kafkaTaskExecutor() {
        return new SimpleAsyncTaskExecutor("kafka-");
    }
}