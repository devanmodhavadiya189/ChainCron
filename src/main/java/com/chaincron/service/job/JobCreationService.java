package com.chaincron.service.job;

import com.chaincron.config.AppProperties;
import com.chaincron.domain.entity.Job;
import com.chaincron.domain.entity.User;
import com.chaincron.domain.enums.JobStatus;
import com.chaincron.domain.repository.JobRepository;
import com.chaincron.domain.repository.UserRepository;
import com.chaincron.dto.request.CreateJobRequest;
import com.chaincron.service.credit.CreditService;
import com.chaincron.util.abi.AbiItem;
import com.chaincron.util.abi.AbiParser;
import com.chaincron.util.abi.CalldataEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobCreationService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CreditService creditService;
    private final CalldataEncoder calldataEncoder;
    private final AbiParser abiParser;
    private final SlotCounter slotCounter;
    private final AppProperties appProperties;

    @Transactional
    public Long createJob(Long userId, CreateJobRequest request) {
        OffsetDateTime scheduledAtUtc = resolveScheduledAt(request.getScheduledAt(), request.getUserTimezone());
        byte[] calldata = calldataEncoder.encode(request.getAbiJson(), request.getFunctionName(), request.getParams());
        AbiItem abiFunction = abiParser.findFunction(request.getAbiJson(), request.getFunctionName());
        String functionSig = calldataEncoder.buildSignature(
                request.getFunctionName(),
                abiFunction.getInputs() == null ? Collections.emptyList() : abiFunction.getInputs()
        );

        BigDecimal totalCost = calculateTotalCost(request.getMaxFeePerGas(), request.getGasLimit());
        long assignedSlot = slotCounter.slotForScheduledAt(
                scheduledAtUtc, appProperties.getScheduler().getPollIntervalSeconds());

        User userRef = userRepository.getReferenceById(userId);
        Job job = Job.builder()
                .user(userRef)
                .contractAddress(request.getContractAddress())
                .functionSig(functionSig)
                .encodedCalldata(calldata)
                .scheduledAt(scheduledAtUtc)
                .userTimezone(request.getUserTimezone())
                .gasLimit(request.getGasLimit())
                .maxFeePerGas(request.getMaxFeePerGas())
                .maxPriorityFeePerGas(request.getMaxPriorityFeePerGas())
                .chargedWei(totalCost)
                .assignedSlot(assignedSlot)
                .status(JobStatus.PENDING)
                .build();

        job = jobRepository.save(job);

        creditService.deduct(userId, totalCost, job.getId());

        log.info("Job created: id={} userId={} slot={} cost={} wei", job.getId(), userId, assignedSlot, totalCost);
        return job.getId();
    }

    public BigDecimal calculateTotalCost(BigDecimal maxFeePerGas, BigDecimal gasLimit) {
        BigDecimal gasCost = maxFeePerGas.multiply(gasLimit);
        return gasCost.add(appProperties.getPlatform().getFeeWei());
    }

    private OffsetDateTime resolveScheduledAt(String scheduledAt, String userTimezone) {
        try {
            ZoneId zone = ZoneId.of(userTimezone);
            LocalDateTime localDt = LocalDateTime.parse(scheduledAt);
            return localDt.atZone(zone).toOffsetDateTime();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid scheduledAt or userTimezone: " + scheduledAt + " / " + userTimezone, e);
        }
    }
}
