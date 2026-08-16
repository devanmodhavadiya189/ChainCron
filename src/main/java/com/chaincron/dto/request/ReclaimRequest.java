package com.chaincron.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReclaimRequest {

    @NotBlank
    @Pattern(regexp = "^0x[0-9a-fA-F]{40}$", message = "Must be a valid Ethereum address (0x + 40 hex chars)")
    private String toAddress;
}
