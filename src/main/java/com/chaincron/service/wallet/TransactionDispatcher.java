package com.chaincron.service.wallet;

import com.chaincron.service.kafka.JobExecutionDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionDispatcher implements JobExecutionDispatcher {

    private final ExecutorService jobWorkerPool;
    private final TransactionSigner signer;
    private final JobStatusWriter statusWriter;
    private final Web3j web3j;

    @Override
    public void dispatch(Long jobId) {
        log.info("Dispatching jobId={} to worker pool", jobId);
        jobWorkerPool.submit(new SendWorker(jobId, signer, statusWriter, web3j));
    }
}
