package com.chaincron.service.wallet;

import com.chaincron.config.AppProperties;
import com.chaincron.domain.entity.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionSigner {

    private final AppProperties appProperties;
    private final NonceManager nonceManager;

    private volatile Credentials credentials;

    public SignedTransaction sign(Job job) {
        BigInteger nonce = nonceManager.nextNonce();
        return signWithNonce(job, nonce);
    }

    public SignedTransaction signWithNonce(Job job, BigInteger nonce) {
        Credentials creds = loadCredentials();
        long chainId = appProperties.getEthereum().getChainId();

        String hexCalldata = Numeric.toHexString(job.getEncodedCalldata());

        RawTransaction rawTx = RawTransaction.createTransaction(
                chainId,
                nonce,
                job.getGasLimit().toBigIntegerExact(),
                job.getContractAddress(),
                BigInteger.ZERO,
                hexCalldata,
                job.getMaxPriorityFeePerGas().toBigIntegerExact(),
                job.getMaxFeePerGas().toBigIntegerExact()
        );

        byte[] signedBytes = TransactionEncoder.signMessage(rawTx, chainId, creds);
        String signedHex = Numeric.toHexString(signedBytes);

        log.debug("Signed tx: jobId={} nonce={} to={}", job.getId(), nonce, job.getContractAddress());
        return new SignedTransaction(nonce, signedHex);
    }

    private Credentials loadCredentials() {
        if (credentials == null) {
            synchronized (this) {
                if (credentials == null) {
                    String privateKey = appProperties.getEthereum().getPlatformWalletPrivateKey();
                    credentials = Credentials.create(privateKey);
                    log.info("Platform wallet loaded: address={}", credentials.getAddress());
                }
            }
        }
        return credentials;
    }
}
