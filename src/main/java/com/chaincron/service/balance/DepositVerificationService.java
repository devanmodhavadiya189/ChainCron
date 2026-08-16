package com.chaincron.service.balance;

import com.chaincron.config.AppProperties;
import com.chaincron.service.credit.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositVerificationService {

    private final Web3j web3j;
    private final CreditService creditService;
    private final AppProperties appProperties;

    public void verifyAndCredit(Long userId, String txHash) {
        String platformWallet = appProperties.getEthereum().getPlatformWalletAddress();

        Transaction tx = fetchTransaction(txHash);

        if (!platformWallet.equalsIgnoreCase(tx.getTo())) {
            throw new IllegalArgumentException(
                    "Transaction recipient is not the platform wallet — cannot credit");
        }

        Optional<TransactionReceipt> receiptOpt = fetchReceipt(txHash);
        if (receiptOpt.isEmpty()) {
            throw new IllegalStateException("Transaction is not yet mined — please wait and retry");
        }

        TransactionReceipt receipt = receiptOpt.get();
        if (!"0x1".equalsIgnoreCase(receipt.getStatus())) {
            throw new IllegalStateException("Transaction was reverted on-chain — cannot credit");
        }

        BigDecimal amountWei = new BigDecimal(tx.getValue());
        creditService.deposit(userId, amountWei, txHash);
        log.info("Manual deposit verified and credited: userId={} txHash={} amount={} wei",
                userId, txHash, amountWei);
    }

    private Transaction fetchTransaction(String txHash) {
        try {
            return web3j.ethGetTransactionByHash(txHash)
                    .send()
                    .getTransaction()
                    .orElseThrow(() -> new IllegalArgumentException("Transaction not found on-chain: " + txHash));
        } catch (java.io.IOException e) {
            throw new RuntimeException("Network error while fetching transaction: " + txHash, e);
        }
    }

    private Optional<TransactionReceipt> fetchReceipt(String txHash) {
        try {
            return web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Network error while fetching receipt: " + txHash, e);
        }
    }
}
