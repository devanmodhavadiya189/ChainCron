package com.chaincron.domain.repository;

import com.chaincron.domain.entity.CreditTransaction;
import com.chaincron.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

    Page<CreditTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(ct.amountWei), 0)
            FROM CreditTransaction ct
            WHERE ct.user.id = :userId
              AND ct.type = com.chaincron.domain.enums.TransactionType.DEPOSIT
            """)
    BigDecimal sumDepositsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT ct FROM CreditTransaction ct
            WHERE ct.txHash = :txHash
              AND ct.type = com.chaincron.domain.enums.TransactionType.DEPOSIT
            """)
    Optional<CreditTransaction> findDepositByTxHash(@Param("txHash") String txHash);

    boolean existsByTxHashAndType(String txHash, TransactionType type);
}
