package com.habittracker.habit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.habit-notifications}")
    private String habitNotificationsTopic;

    @Value("${kafka.topics.habit-completions}")
    private String habitCompletionsTopic;

    @Bean
    public NewTopics habitTopics() {
        return new KafkaAdmin.NewTopics(
            TopicBuilder.name(habitNotificationsTopic)
                .partitions(3)
                .replicas(1)
                .build(),
            TopicBuilder.name(habitCompletionsTopic)
                .partitions(3)
                .replicas(1)
                .build()
        );
    }
}
