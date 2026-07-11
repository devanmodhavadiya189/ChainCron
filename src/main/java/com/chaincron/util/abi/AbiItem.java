package com.chaincron.util.abi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbiItem {
    private String name;
    private String type;
    private String stateMutability;
    private List<AbiInputParam> inputs;

    public boolean isFunction() {
        return "function".equals(type);
    }
}
