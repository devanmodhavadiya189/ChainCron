package com.chaincron.domain.entity;

import com.chaincron.domain.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "contract_address", nullable = false, length = 42)
    private String contractAddress;

    @Column(name = "function_sig", nullable = false, length = 512)
    private String functionSig;

    @Column(name = "encoded_calldata", nullable = false, columnDefinition = "BYTEA")
    private byte[] encodedCalldata;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(name = "user_timezone", nullable = false, length = 64)
    private String userTimezone;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "job_status")
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "tx_hash", length = 66)
    private String txHash;

    @Column(name = "gas_limit", nullable = false, precision = 38, scale = 0)
    private BigDecimal gasLimit;

    @Column(name = "gas_used", precision = 38, scale = 0)
    private BigDecimal gasUsed;

    @Column(name = "max_fee_per_gas", nullable = false, precision = 38, scale = 0)
    private BigDecimal maxFeePerGas;

    @Column(name = "max_priority_fee_per_gas", nullable = false, precision = 38, scale = 0)
    private BigDecimal maxPriorityFeePerGas;

    @Column(name = "charged_wei", precision = 38, scale = 0)
    private BigDecimal chargedWei;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "assigned_slot", nullable = false)
    private Long assignedSlot;

    @Column(name = "revert_reason", columnDefinition = "TEXT")
    private String revertReason;

    @Column(name = "block_number")
    private Long blockNumber;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
