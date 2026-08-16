package com.chaincron.service.confirmation;

import com.chaincron.domain.entity.Job;
import com.chaincron.domain.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmationPollerService {

    private final JobRepository jobRepository;
    private final Web3j web3j;
    private final ConfirmationTransactionHelper txHelper;

    @Scheduled(fixedDelayString = "PT${chaincron.scheduler.confirmation-check-delay-seconds:20}S")
    public void pollConfirmations() {
        List<Job> submittedJobs = jobRepository.findSubmittedJobsWithTxHash();
        if (submittedJobs.isEmpty()) {
            return;
        }

        log.debug("Polling confirmations for {} submitted jobs", submittedJobs.size());

        for (Job job : submittedJobs) {
            processJob(job);
        }
    }

    private void processJob(Job job) {
        try {
            Optional<TransactionReceipt> receiptOpt = fetchReceipt(job.getTxHash());

            if (receiptOpt.isEmpty()) {
                log.debug("jobId={} txHash={} still pending in mempool", job.getId(), job.getTxHash());
                return;
            }

            TransactionReceipt receipt = receiptOpt.get();

            if (isSuccess(receipt)) {
                BigDecimal effectiveGasPrice = new BigDecimal(
                        org.web3j.utils.Numeric.decodeQuantity(receipt.getEffectiveGasPrice()));
                BigDecimal gasUsed = new BigDecimal(receipt.getGasUsed());
                Long blockNumber = receipt.getBlockNumber().longValue();

                txHelper.handleConfirmed(
                        job.getId(),
                        receipt.getTransactionHash(),
                        blockNumber,
                        effectiveGasPrice,
                        gasUsed
                );

            } else {
                log.warn("Transaction reverted on-chain: jobId={} txHash={}", job.getId(), job.getTxHash());
                txHelper.handleFailed(job.getId(), "Transaction reverted on-chain at block " + receipt.getBlockNumberRaw());
            }

        } catch (IOException e) {
            log.error("Network error while polling receipt for jobId={}: {}", job.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing confirmation for jobId={}", job.getId(), e);
        }
    }

    private Optional<TransactionReceipt> fetchReceipt(String txHash) throws IOException {
        EthGetTransactionReceipt response = web3j.ethGetTransactionReceipt(txHash).send();
        return response.getTransactionReceipt();
    }

    private boolean isSuccess(TransactionReceipt receipt) {
        String status = receipt.getStatus();
        return "0x1".equalsIgnoreCase(status) || "1".equalsIgnoreCase(status);
    }
}
