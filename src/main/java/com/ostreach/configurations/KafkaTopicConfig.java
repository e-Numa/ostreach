package com.ostreach.configurations;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

public class KafkaTopicConfig {
    @Bean
    public NewTopic projectTopic(){
        return TopicBuilder.name("driverUnavailableStatus")
                .build();
    }
    @Bean
    public NewTopic newProjectTopic(){
        return TopicBuilder.name("driverAvailableStatus")
                .build();
    }

    @Bean
    public NewTopic newProjectTopics(){
        return TopicBuilder.name("assignedTask")
                .build();
    }
}
