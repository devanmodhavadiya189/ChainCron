package com.chaincron.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobKafkaConsumer {

    private final ConsumerTransactionHelper txHelper;
    private final JobExecutionDispatcher dispatcher;

    @KafkaListener(
            topics = "${chaincron.kafka.topic.job-executions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onJobQueued(ConsumerRecord<String, String> record, Acknowledgment ack) {
        Long jobId = null;
        try {
            jobId = Long.parseLong(record.value().trim());
            log.info("Kafka message received: jobId={} partition={} offset={}",
                    jobId, record.partition(), record.offset());

            boolean marked = txHelper.tryMarkExecuting(jobId);

            if (marked) {
                dispatcher.dispatch(jobId);
            }

        } catch (NumberFormatException e) {
            log.error("Unparseable Kafka message value='{}' — discarding", record.value());
        } finally {
            ack.acknowledge();
        }
    }
}
