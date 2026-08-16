package com.chaincron.service.confirmation;

import com.chaincron.config.AppProperties;
import com.chaincron.domain.entity.Job;
import com.chaincron.domain.repository.JobRepository;
import com.chaincron.exception.ResourceNotFoundException;
import com.chaincron.service.credit.CreditService;
import com.chaincron.service.wallet.JobStatusWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
class ConfirmationTransactionHelper {

    private final JobRepository jobRepository;
    private final CreditService creditService;
    private final JobStatusWriter statusWriter;
    private final AppProperties appProperties;

    @Transactional
    void handleConfirmed(Long jobId, String txHash, Long blockNumber, BigDecimal effectiveGasPrice, BigDecimal gasUsed) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        statusWriter.markConfirmed(jobId, txHash, blockNumber, gasUsed);

        BigDecimal actualGasCost = effectiveGasPrice.multiply(gasUsed);
        BigDecimal platformFeeWei = appProperties.getPlatform().getFeeWei();
        BigDecimal refundAmount = job.getChargedWei()
                .subtract(actualGasCost)
                .subtract(platformFeeWei);

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            creditService.refundJobGas(job.getUser().getId(), jobId, refundAmount);
            log.info("Gas refund issued: jobId={} refund={} wei", jobId, refundAmount);
        }
    }

    @Transactional
    void handleFailed(Long jobId, String reason) {
        statusWriter.markFailed(jobId, reason);
    }
}
