package com.chaincron.service.scheduler;

import com.chaincron.domain.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class SchedulerTransactionHelper {

    private final JobRepository jobRepository;

    @Transactional
    List<Long> markDueJobsQueued(long currentSlot) {
        List<Long> candidateIds = jobRepository
                .findPendingJobsUpToSlot(currentSlot)
                .stream()
                .map(job -> job.getId())
                .toList();

        List<Long> queued = candidateIds.stream()
                .filter(id -> jobRepository.markQueuedIfPending(id) == 1)
                .toList();

        if (!queued.isEmpty()) {
            log.info("Slot={} marked {} jobs QUEUED: {}", currentSlot, queued.size(), queued);
        }
        return queued;
    }
}
