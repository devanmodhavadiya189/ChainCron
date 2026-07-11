package com.chaincron.service.wallet;

import com.chaincron.domain.entity.Job;
import com.chaincron.domain.enums.JobStatus;
import com.chaincron.domain.repository.JobRepository;
import com.chaincron.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobStatusWriter {

    private final JobRepository jobRepository;

    @Transactional
    public Job loadJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
    }

    @Transactional
    public void markSubmitted(Long jobId, String txHash) {
        Job job = loadJobForUpdate(jobId);
        job.setStatus(JobStatus.SUBMITTED);
        job.setTxHash(txHash);
        jobRepository.save(job);
        log.info("Job SUBMITTED: id={} txHash={}", jobId, txHash);
    }

    @Transactional
    public void markFailed(Long jobId, String reason) {
        Job job = loadJobForUpdate(jobId);
        job.setStatus(JobStatus.FAILED);
        job.setRevertReason(reason);
        jobRepository.save(job);
        log.warn("Job FAILED: id={} reason={}", jobId, reason);
    }

    @Transactional
    public void markConfirmed(Long jobId, String txHash, Long blockNumber, java.math.BigDecimal gasUsed) {
        Job job = loadJobForUpdate(jobId);
        job.setStatus(JobStatus.CONFIRMED);
        job.setTxHash(txHash);
        job.setBlockNumber(blockNumber);
        job.setGasUsed(gasUsed);
        jobRepository.save(job);
        log.info("Job CONFIRMED: id={} block={} gasUsed={}", jobId, blockNumber, gasUsed);
    }

    private Job loadJobForUpdate(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
    }
}
