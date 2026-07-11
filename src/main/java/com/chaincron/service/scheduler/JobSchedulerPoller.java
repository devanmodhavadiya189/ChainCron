package com.chaincron.service.scheduler;

import com.chaincron.service.job.SlotCounter;
import com.chaincron.service.kafka.JobEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSchedulerPoller {

    private final SlotCounter slotCounter;
    private final SchedulerTransactionHelper txHelper;
    private final JobEventPublisher eventPublisher;

    @Scheduled(fixedDelayString = "PT${chaincron.scheduler.poll-interval-seconds:15}S")
    public void poll() {
        long currentSlot = slotCounter.advanceSlot();
        log.debug("Scheduler poll: slot={}", currentSlot);

        List<Long> queuedIds = txHelper.markDueJobsQueued(currentSlot);

        queuedIds.forEach(eventPublisher::publishJobQueued);
    }
}
