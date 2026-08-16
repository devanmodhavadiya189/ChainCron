package com.chaincron.controller;

import com.chaincron.domain.entity.Job;
import com.chaincron.domain.repository.JobRepository;
import com.chaincron.dto.request.CreateJobRequest;
import com.chaincron.dto.response.JobResponse;
import com.chaincron.exception.ResourceNotFoundException;
import com.chaincron.security.SecurityUtils;
import com.chaincron.service.job.JobCreationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobCreationService jobCreationService;
    private final JobRepository jobRepository;

    @PostMapping
    public ResponseEntity<Map<String, Long>> createJob(@Valid @RequestBody CreateJobRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Long jobId = jobCreationService.createJob(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("jobId", jobId));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> listJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtils.currentUserId();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobResponse> jobs = jobRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(JobResponse::from);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        Long userId = SecurityUtils.currentUserId();
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", id));

        if (!job.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(JobResponse.from(job));
    }
}
