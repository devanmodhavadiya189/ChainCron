package com.chaincron.service.credit;

import com.chaincron.domain.entity.CreditTransaction;
import com.chaincron.domain.entity.Job;
import com.chaincron.domain.entity.User;
import com.chaincron.domain.enums.TransactionType;
import com.chaincron.domain.repository.CreditTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
class LedgerWriter {

    private final CreditTransactionRepository ledgerRepo;

    CreditTransaction write(User user, Job job, TransactionType type, BigDecimal amountWei, String txHash, String note) {
        CreditTransaction entry = CreditTransaction.builder()
                .user(user)
                .job(job)
                .type(type)
                .amountWei(amountWei)
                .txHash(txHash)
                .note(note)
                .build();
        return ledgerRepo.save(entry);
    }

    CreditTransaction write(User user, TransactionType type, BigDecimal amountWei, String txHash, String note) {
        return write(user, null, type, amountWei, txHash, note);
    }
}
