package com.chaincron.service.kafka;

import com.chaincron.domain.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class ConsumerTransactionHelper {

    private final JobRepository jobRepository;

    @Transactional
    boolean tryMarkExecuting(Long jobId) {
        int updated = jobRepository.markExecutingIfQueued(jobId);
        if (updated == 0) {
            log.warn("Idempotent skip: jobId={} not in QUEUED state (already processed or redelivered)", jobId);
            return false;
        }
        return true;
    }
}
