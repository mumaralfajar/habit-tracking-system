package com.habitsystem.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class TransactionConfig {

    @Bean
    @Primary
    public PlatformTransactionManager chainedTransactionManager(
            EntityManagerFactory entityManagerFactory,
            KafkaTransactionManager<?, ?> kafkaTransactionManager) {
        
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(entityManagerFactory);
        
        return new ChainedTransactionManager(
                kafkaTransactionManager,
                jpaTransactionManager
        );
    }
}