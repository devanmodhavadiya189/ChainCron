package com.chaincron.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient balance: required %s wei, available %s wei", required, available));
    }
}
