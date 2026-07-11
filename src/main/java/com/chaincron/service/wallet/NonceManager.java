package com.chaincron.service.wallet;

import com.chaincron.config.AppProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class NonceManager {

    private final Web3j web3j;
    private final AppProperties appProperties;

    private final AtomicLong nonce = new AtomicLong(0);

    @PostConstruct
    public void init() {
        sync();
    }

    public BigInteger nextNonce() {
        return BigInteger.valueOf(nonce.getAndIncrement());
    }

    public void sync() {
        String address = appProperties.getEthereum().getPlatformWalletAddress();
        try {
            BigInteger onChainNonce = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING)
                    .send()
                    .getTransactionCount();
            nonce.set(onChainNonce.longValue());
            log.info("NonceManager synced: next nonce={} for address={}", onChainNonce, address);
        } catch (IOException e) {
            log.error("Failed to sync nonce from chain — retaining current value={}", nonce.get(), e);
        }
    }
}
