package com.chaincron.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BalanceResponse {
    private BigDecimal creditBalanceWei;
    private String walletAddress;
}
