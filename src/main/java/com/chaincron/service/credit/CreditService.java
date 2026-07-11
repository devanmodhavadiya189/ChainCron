package com.chaincron.service.credit;

import com.chaincron.domain.entity.Job;
import com.chaincron.domain.entity.User;
import com.chaincron.domain.enums.TransactionType;
import com.chaincron.domain.repository.CreditTransactionRepository;
import com.chaincron.domain.repository.JobRepository;
import com.chaincron.domain.repository.UserRepository;
import com.chaincron.exception.InsufficientBalanceException;
import com.chaincron.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final CreditTransactionRepository ledgerRepo;
    private final LedgerWriter ledgerWriter;

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId))
                .getCreditBalanceWei();
    }

    @Transactional
    public void deposit(Long userId, BigDecimal amountWei, String txHash) {
        if (ledgerRepo.existsByTxHashAndType(txHash, TransactionType.DEPOSIT)) {
            log.warn("Duplicate deposit ignored: txHash={}", txHash);
            return;
        }
        User user = lockedUser(userId);
        user.setCreditBalanceWei(user.getCreditBalanceWei().add(amountWei));
        userRepository.save(user);
        ledgerWriter.write(user, TransactionType.DEPOSIT, amountWei, txHash, "Sepolia ETH deposit");
        log.info("Deposited {} wei for userId={}", amountWei, userId);
    }

    @Transactional
    public void deduct(Long userId, BigDecimal amountWei, Long jobId) {
        User user = lockedUser(userId);
        BigDecimal current = user.getCreditBalanceWei();
        if (current.compareTo(amountWei) < 0) {
            throw new InsufficientBalanceException(amountWei, current);
        }
        user.setCreditBalanceWei(current.subtract(amountWei));
        userRepository.save(user);
        Job job = jobRepository.getReferenceById(jobId);
        ledgerWriter.write(user, job, TransactionType.CHARGE, amountWei, null, "Job scheduling charge");
        log.info("Charged {} wei for userId={} jobId={}", amountWei, userId, jobId);
    }

    @Transactional
    public void refundJobGas(Long userId, Long jobId, BigDecimal refundAmountWei) {
        if (refundAmountWei.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        User user = lockedUser(userId);
        user.setCreditBalanceWei(user.getCreditBalanceWei().add(refundAmountWei));
        userRepository.save(user);
        Job job = jobRepository.getReferenceById(jobId);
        ledgerWriter.write(user, job, TransactionType.REFUND, refundAmountWei, null, "Unspent gas refund");
        log.info("Refunded {} wei for userId={} jobId={}", refundAmountWei, userId, jobId);
    }

    @Transactional
    public BigDecimal reclaimFullBalance(Long userId) {
        User user = lockedUser(userId);
        BigDecimal balance = user.getCreditBalanceWei();
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        user.setCreditBalanceWei(BigDecimal.ZERO);
        userRepository.save(user);
        ledgerWriter.write(user, TransactionType.REFUND, balance, null, "User balance reclaim");
        log.info("Reclaim initiated for userId={} amount={} wei", userId, balance);
        return balance;
    }

    @Transactional(readOnly = true)
    public List<com.chaincron.domain.entity.CreditTransaction> getLedger(Long userId, int page, int size) {
        return ledgerRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size)).getContent();
    }

    private User lockedUser(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
