package com.chaincron.service.wallet;

import com.chaincron.domain.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.io.IOException;

@Slf4j
class SendWorker implements Runnable {

    private static final int[] BACKOFF_MS = {1_000, 2_000, 4_000};
    private static final int MAX_ATTEMPTS = 3;

    private final Long jobId;
    private final TransactionSigner signer;
    private final JobStatusWriter statusWriter;
    private final Web3j web3j;

    SendWorker(Long jobId, TransactionSigner signer, JobStatusWriter statusWriter, Web3j web3j) {
        this.jobId = jobId;
        this.signer = signer;
        this.statusWriter = statusWriter;
        this.web3j = web3j;
    }

    @Override
    public void run() {
        Job job;
        try {
            job = statusWriter.loadJob(jobId);
        } catch (Exception e) {
            log.error("SendWorker: could not load jobId={}", jobId, e);
            return;
        }

        SignedTransaction signed;
        try {
            signed = signer.sign(job);
        } catch (Exception e) {
            statusWriter.markFailed(jobId, "Signing failed: " + e.getMessage());
            return;
        }

        attemptSend(job, signed);
    }

    private void attemptSend(Job job, SignedTransaction signed) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                EthSendTransaction response = web3j
                        .ethSendRawTransaction(signed.signedHex())
                        .send();

                if (response.hasError()) {
                    String errorMsg = response.getError().getMessage();
                    log.warn("Node rejected tx: jobId={} nonce={} error={}", jobId, signed.nonce(), errorMsg);
                    statusWriter.markFailed(jobId, "Node rejected: " + errorMsg);
                    return;
                }

                String txHash = response.getTransactionHash();
                statusWriter.markSubmitted(jobId, txHash);
                return;

            } catch (IOException e) {
                log.warn("Send attempt {}/{} failed for jobId={} nonce={}: {}",
                        attempt + 1, MAX_ATTEMPTS, jobId, signed.nonce(), e.getMessage());

                if (attempt < MAX_ATTEMPTS - 1) {
                    sleep(BACKOFF_MS[attempt]);
                } else {
                    statusWriter.markFailed(jobId, "Send failed after " + MAX_ATTEMPTS + " attempts: " + e.getMessage());
                }
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
