package com.chaincron.util.abi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AbiParser {

    private final ObjectMapper objectMapper;

    public List<AbiItem> parse(String abiJson) {
        try {
            return objectMapper.readValue(abiJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ABI JSON: " + e.getMessage(), e);
        }
    }

    public List<String> functionNames(String abiJson) {
        return parse(abiJson).stream()
                .filter(AbiItem::isFunction)
                .map(AbiItem::getName)
                .distinct()
                .toList();
    }

    public AbiItem findFunction(String abiJson, String functionName) {
        return parse(abiJson).stream()
                .filter(AbiItem::isFunction)
                .filter(item -> functionName.equals(item.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Function '" + functionName + "' not found in ABI"));
    }
}
