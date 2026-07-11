package com.chaincron.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final AppProperties appProperties;

    @Bean
    public NewTopic jobExecutionsTopic() {
        return TopicBuilder
                .name(appProperties.getKafka().getTopic().getJobExecutions())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
