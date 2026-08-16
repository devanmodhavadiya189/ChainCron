package com.chaincron.controller;

import com.chaincron.domain.entity.CreditTransaction;
import com.chaincron.domain.entity.User;
import com.chaincron.domain.repository.UserRepository;
import com.chaincron.dto.request.DepositRequest;
import com.chaincron.dto.request.ReclaimRequest;
import com.chaincron.dto.response.BalanceResponse;
import com.chaincron.exception.ResourceNotFoundException;
import com.chaincron.security.SecurityUtils;
import com.chaincron.service.balance.DepositVerificationService;
import com.chaincron.service.balance.ReclaimService;
import com.chaincron.service.credit.CreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final CreditService creditService;
    private final DepositVerificationService depositVerificationService;
    private final ReclaimService reclaimService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<BalanceResponse> getBalance() {
        Long userId = SecurityUtils.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return ResponseEntity.ok(BalanceResponse.builder()
                .creditBalanceWei(user.getCreditBalanceWei())
                .walletAddress(user.getWalletAddress())
                .build());
    }

    @GetMapping("/ledger")
    public ResponseEntity<List<CreditTransaction>> getLedger(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(creditService.getLedger(userId, page, size));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, String>> deposit(@Valid @RequestBody DepositRequest request) {
        Long userId = SecurityUtils.currentUserId();
        depositVerificationService.verifyAndCredit(userId, request.getTxHash());
        return ResponseEntity.ok(Map.of("status", "credited", "txHash", request.getTxHash()));
    }

    @PostMapping("/reclaim")
    public ResponseEntity<Map<String, String>> reclaim(@Valid @RequestBody ReclaimRequest request) {
        Long userId = SecurityUtils.currentUserId();
        String txHash = reclaimService.reclaim(userId, request.getToAddress());
        return ResponseEntity.ok(Map.of("status", "reclaim_initiated", "txHash", txHash));
    }

    @PatchMapping("/wallet")
    public ResponseEntity<Void> registerWallet(@RequestParam String address) {
        Long userId = SecurityUtils.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setWalletAddress(address.toLowerCase());
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
