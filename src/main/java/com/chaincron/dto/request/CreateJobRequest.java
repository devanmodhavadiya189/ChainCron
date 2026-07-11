package com.chaincron.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateJobRequest {

    @NotBlank
    private String contractAddress;

    @NotBlank
    private String abiJson;

    @NotBlank
    private String functionName;

    @NotNull
    private List<String> params;

    @NotBlank
    private String scheduledAt;

    @NotBlank
    private String userTimezone;

    @NotNull
    @DecimalMin("21000")
    private BigDecimal gasLimit;

    @NotNull
    @DecimalMin("1")
    private BigDecimal maxFeePerGas;

    @NotNull
    @DecimalMin("0")
    private BigDecimal maxPriorityFeePerGas;
}
