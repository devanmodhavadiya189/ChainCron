package com.chaincron.service.kafka;

import com.chaincron.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AppProperties appProperties;

    public void publishJobQueued(Long jobId) {
        String topic = appProperties.getKafka().getTopic().getJobExecutions();
        kafkaTemplate.send(topic, String.valueOf(jobId))
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish jobId={} to Kafka topic={}", jobId, topic, ex);
                    } else {
                        log.debug("Published jobId={} to topic={} partition={} offset={}",
                                jobId, topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
