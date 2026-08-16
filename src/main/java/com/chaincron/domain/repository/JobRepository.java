package com.chaincron.domain.repository;

import com.chaincron.domain.entity.Job;
import com.chaincron.domain.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("""
            SELECT j FROM Job j
            WHERE j.assignedSlot <= :currentSlot
              AND j.status = com.chaincron.domain.enums.JobStatus.PENDING
            ORDER BY j.assignedSlot ASC
            """)
    List<Job> findPendingJobsUpToSlot(@Param("currentSlot") long currentSlot);

    @Modifying
    @Query("""
            UPDATE Job j
            SET j.status = com.chaincron.domain.enums.JobStatus.QUEUED
            WHERE j.id = :id
              AND j.status = com.chaincron.domain.enums.JobStatus.PENDING
            """)
    int markQueuedIfPending(@Param("id") Long id);

    @Modifying
    @Query("""
            UPDATE Job j
            SET j.status = com.chaincron.domain.enums.JobStatus.EXECUTING,
                j.attemptCount = j.attemptCount + 1
            WHERE j.id = :id
              AND j.status = com.chaincron.domain.enums.JobStatus.QUEUED
            """)
    int markExecutingIfQueued(@Param("id") Long id);

    @Query("""
            SELECT j FROM Job j
            WHERE j.status = com.chaincron.domain.enums.JobStatus.EXECUTING
              AND j.txHash IS NULL
              AND j.updatedAt < :threshold
            """)
    List<Job> findStaleExecutingWithoutTxHash(@Param("threshold") OffsetDateTime threshold);

    @Query("""
            SELECT j FROM Job j
            WHERE j.status = com.chaincron.domain.enums.JobStatus.EXECUTING
              AND j.txHash IS NOT NULL
            """)
    List<Job> findExecutingWithTxHash();

    @Query("""
            SELECT j FROM Job j
            WHERE j.status = com.chaincron.domain.enums.JobStatus.SUBMITTED
            ORDER BY j.assignedSlot ASC
            """)
    List<Job> findAllSubmitted();

    @Query("""
            SELECT j FROM Job j
            WHERE j.status = com.chaincron.domain.enums.JobStatus.SUBMITTED
              AND j.txHash = :txHash
            """)
    java.util.Optional<Job> findSubmittedByTxHash(@Param("txHash") String txHash);

    boolean existsByUserIdAndStatus(Long userId, JobStatus status);

    @Query("""
            SELECT j FROM Job j
            WHERE j.status = com.chaincron.domain.enums.JobStatus.SUBMITTED
              AND j.txHash IS NOT NULL
            """)
    List<Job> findSubmittedJobsWithTxHash();

    @Modifying
    @Query("""
            UPDATE Job j
            SET j.status = com.chaincron.domain.enums.JobStatus.PENDING
            WHERE j.id = :id
              AND j.status = com.chaincron.domain.enums.JobStatus.EXECUTING
              AND j.txHash IS NULL
            """)
    int resetExecutingToPending(@Param("id") Long id);

    @Modifying
    @Query("""
            UPDATE Job j
            SET j.status = com.chaincron.domain.enums.JobStatus.SUBMITTED
            WHERE j.id = :id
              AND j.status = com.chaincron.domain.enums.JobStatus.EXECUTING
              AND j.txHash IS NOT NULL
            """)
    int markExecutingAsSubmitted(@Param("id") Long id);
}
