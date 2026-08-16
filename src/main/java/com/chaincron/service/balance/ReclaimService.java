package com.chaincron.service.balance;

import com.chaincron.config.AppProperties;
import com.chaincron.service.credit.CreditService;
import com.chaincron.service.wallet.NonceManager;
import com.chaincron.service.wallet.TransactionSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReclaimService {

    private static final BigInteger ETH_TRANSFER_GAS_LIMIT = BigInteger.valueOf(21_000);

    private final Web3j web3j;
    private final CreditService creditService;
    private final NonceManager nonceManager;
    private final AppProperties appProperties;

    public String reclaim(Long userId, String toAddress) {
        BigDecimal balanceWei = creditService.getBalance(userId);
        if (balanceWei.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No balance to reclaim");
        }

        BigDecimal gasCostWei = estimateGasCostWei();
        BigDecimal sendAmountWei = balanceWei.subtract(gasCostWei);

        if (sendAmountWei.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Balance is insufficient to cover the gas cost of the transfer");
        }

        BigDecimal reclaimedFromDb = creditService.reclaimFullBalance(userId);

        String txHash = sendEthTransfer(toAddress, sendAmountWei.toBigInteger());
        log.info("Reclaim sent: userId={} toAddress={} amount={} wei txHash={}", userId, toAddress, sendAmountWei, txHash);
        return txHash;
    }

    private String sendEthTransfer(String toAddress, BigInteger amountWei) {
        try {
            String privateKey = appProperties.getEthereum().getPlatformWalletPrivateKey();
            long chainId = appProperties.getEthereum().getChainId();
            Credentials creds = Credentials.create(privateKey);

            BigInteger currentBaseFee = fetchCurrentBaseFee();
            BigInteger maxPriorityFee = BigInteger.valueOf(1_000_000_000L);
            BigInteger maxFee = currentBaseFee.multiply(BigInteger.TWO).add(maxPriorityFee);

            BigInteger nonce = nonceManager.nextNonce();

            RawTransaction rawTx = RawTransaction.createTransaction(
                    chainId, nonce, ETH_TRANSFER_GAS_LIMIT,
                    toAddress, amountWei, "",
                    maxPriorityFee, maxFee
            );

            byte[] signed = TransactionEncoder.signMessage(rawTx, chainId, creds);
            EthSendTransaction response = web3j.ethSendRawTransaction(Numeric.toHexString(signed)).send();

            if (response.hasError()) {
                throw new RuntimeException("Reclaim tx rejected: " + response.getError().getMessage());
            }
            return response.getTransactionHash();

        } catch (java.io.IOException e) {
            throw new RuntimeException("Network error during reclaim transfer", e);
        }
    }

    private BigDecimal estimateGasCostWei() {
        try {
            BigInteger baseFee = fetchCurrentBaseFee();
            BigInteger priorityFee = BigInteger.valueOf(1_000_000_000L);
            BigInteger maxFee = baseFee.multiply(BigInteger.TWO).add(priorityFee);
            return new BigDecimal(maxFee.multiply(ETH_TRANSFER_GAS_LIMIT));
        } catch (Exception e) {
            return new BigDecimal("500000000000000");
        }
    }

    private BigInteger fetchCurrentBaseFee() throws java.io.IOException {
        return web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
                .send()
                .getBlock()
                .getBaseFeePerGas();
    }
}
