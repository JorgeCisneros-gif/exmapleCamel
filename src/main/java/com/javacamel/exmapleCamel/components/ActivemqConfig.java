package com.javacamel.exmapleCamel.components;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivemqConfig {

    @Bean
    public ConnectionFactory connectionFactory() {

        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory("tcp://localhost:61616");

        connectionFactory.setUserName("admin");
        connectionFactory.setPassword("admin");
        connectionFactory.setTrustAllPackages(true);
        return connectionFactory;
    }
}