package com.chaincron.dto.response;

import com.chaincron.domain.entity.Job;
import com.chaincron.domain.enums.JobStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class JobResponse {

    private Long id;
    private String contractAddress;
    private String functionSig;
    private JobStatus status;
    private OffsetDateTime scheduledAt;
    private String userTimezone;
    private BigDecimal gasLimit;
    private BigDecimal maxFeePerGas;
    private BigDecimal maxPriorityFeePerGas;
    private BigDecimal chargedWei;
    private String txHash;
    private Long blockNumber;
    private BigDecimal gasUsed;
    private String revertReason;
    private Long assignedSlot;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static JobResponse from(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .contractAddress(job.getContractAddress())
                .functionSig(job.getFunctionSig())
                .status(job.getStatus())
                .scheduledAt(job.getScheduledAt())
                .userTimezone(job.getUserTimezone())
                .gasLimit(job.getGasLimit())
                .maxFeePerGas(job.getMaxFeePerGas())
                .maxPriorityFeePerGas(job.getMaxPriorityFeePerGas())
                .chargedWei(job.getChargedWei())
                .txHash(job.getTxHash())
                .blockNumber(job.getBlockNumber())
                .gasUsed(job.getGasUsed())
                .revertReason(job.getRevertReason())
                .assignedSlot(job.getAssignedSlot())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
