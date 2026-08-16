package com.chaincron.service.recovery;

import com.chaincron.config.AppProperties;
import com.chaincron.domain.entity.Job;
import com.chaincron.domain.repository.JobRepository;
import com.chaincron.service.wallet.NonceManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrashRecoveryService {

    private final JobRepository jobRepository;
    private final NonceManager nonceManager;
    private final AppProperties appProperties;

    @PostConstruct
    public void recoverOnStartup() {
        log.info("CrashRecoveryService: running startup recovery sweep");
        nonceManager.sync();
        sweep();
    }

    @Scheduled(fixedDelayString = "PT${chaincron.scheduler.stale-job-sweep-interval-seconds:90}S")
    public void scheduledSweep() {
        log.debug("CrashRecoveryService: running scheduled sweep");
        sweep();
    }

    @Transactional
    public void sweep() {
        recoverStaleExecutingNoTxHash();
        recoverExecutingWithTxHash();
    }

    private void recoverStaleExecutingNoTxHash() {
        int timeoutSeconds = appProperties.getScheduler().getStaleJobTimeoutSeconds();
        OffsetDateTime threshold = OffsetDateTime.now().minusSeconds(timeoutSeconds);

        List<Job> stale = jobRepository.findStaleExecutingWithoutTxHash(threshold);
        if (stale.isEmpty()) {
            return;
        }

        log.warn("Recovery: found {} stale EXECUTING jobs with no txHash (older than {}s) — resetting to PENDING",
                stale.size(), timeoutSeconds);

        for (Job job : stale) {
            int updated = jobRepository.resetExecutingToPending(job.getId());
            if (updated == 1) {
                log.info("Recovery: jobId={} reset EXECUTING → PENDING", job.getId());
            }
        }
    }

    private void recoverExecutingWithTxHash() {
        List<Job> executing = jobRepository.findExecutingWithTxHash();
        if (executing.isEmpty()) {
            return;
        }

        log.warn("Recovery: found {} EXECUTING jobs with txHash — promoting to SUBMITTED for confirmation polling",
                executing.size());

        for (Job job : executing) {
            int updated = jobRepository.markExecutingAsSubmitted(job.getId());
            if (updated == 1) {
                log.info("Recovery: jobId={} txHash={} promoted EXECUTING → SUBMITTED",
                        job.getId(), job.getTxHash());
            }
        }
    }
}
