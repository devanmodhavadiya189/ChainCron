package com.chaincron.service.wallet;

import java.math.BigInteger;

public record SignedTransaction(
        BigInteger nonce,
        String signedHex
) {}
