package com.chaincron.util.abi;

import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.*;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Web3jTypeConverter {

    private static final Pattern FIXED_ARRAY_PATTERN = Pattern.compile("^(.+)\\[(\\d+)]$");
    private static final Pattern DYNAMIC_ARRAY_PATTERN = Pattern.compile("^(.+)\\[]$");

    public Type<?> convert(String solidityType, String value) {
        solidityType = solidityType.trim();

        Matcher fixedArray = FIXED_ARRAY_PATTERN.matcher(solidityType);
        if (fixedArray.matches()) {
            return convertFixedArray(fixedArray.group(1), Integer.parseInt(fixedArray.group(2)), value);
        }

        Matcher dynamicArray = DYNAMIC_ARRAY_PATTERN.matcher(solidityType);
        if (dynamicArray.matches()) {
            return convertDynamicArray(dynamicArray.group(1), value);
        }

        return convertScalar(solidityType, value);
    }

    private Type<?> convertScalar(String type, String value) {
        return switch (type) {
            case "address" -> new Address(value.trim());
            case "bool" -> new Bool(Boolean.parseBoolean(value.trim()));
            case "string" -> new Utf8String(value);
            case "bytes" -> new DynamicBytes(Numeric.hexStringToByteArray(normalizeHex(value)));
            case "uint", "uint256" -> new Uint256(parseBigInt(value));
            case "uint8" -> new Uint8(parseBigInt(value));
            case "uint16" -> new Uint16(parseBigInt(value));
            case "uint32" -> new Uint32(parseBigInt(value));
            case "uint64" -> new Uint64(parseBigInt(value));
            case "uint128" -> new Uint128(parseBigInt(value));
            case "int", "int256" -> new Int256(parseBigInt(value));
            case "int8" -> new Int8(parseBigInt(value));
            case "int16" -> new Int16(parseBigInt(value));
            case "int32" -> new Int32(parseBigInt(value));
            case "int64" -> new Int64(parseBigInt(value));
            case "int128" -> new Int128(parseBigInt(value));
            case "bytes1" -> new Bytes1(toFixedBytes(value, 1));
            case "bytes2" -> new Bytes2(toFixedBytes(value, 2));
            case "bytes4" -> new Bytes4(toFixedBytes(value, 4));
            case "bytes8" -> new Bytes8(toFixedBytes(value, 8));
            case "bytes16" -> new Bytes16(toFixedBytes(value, 16));
            case "bytes32" -> new Bytes32(toFixedBytes(value, 32));
            default -> throw new IllegalArgumentException("Unsupported Solidity type: " + type);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private DynamicArray<?> convertDynamicArray(String elementType, String value) {
        List<String> items = splitArrayValue(value);
        List elements = items.stream().map(v -> convertScalar(elementType, v)).toList();
        return new DynamicArray(resolveClass(elementType), elements);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private DynamicArray<?> convertFixedArray(String elementType, int size, String value) {
        List<String> items = splitArrayValue(value);
        if (items.size() != size) {
            throw new IllegalArgumentException(
                "Expected " + size + " elements for " + elementType + "[" + size + "], got " + items.size());
        }
        List elements = items.stream().map(v -> convertScalar(elementType, v)).toList();
        return new DynamicArray(resolveClass(elementType), elements);
    }

    private Class<? extends Type> resolveClass(String type) {
        return switch (type) {
            case "address" -> Address.class;
            case "bool" -> Bool.class;
            case "string" -> Utf8String.class;
            case "bytes" -> DynamicBytes.class;
            case "uint", "uint256" -> Uint256.class;
            case "uint8" -> Uint8.class;
            case "uint16" -> Uint16.class;
            case "uint32" -> Uint32.class;
            case "uint64" -> Uint64.class;
            case "uint128" -> Uint128.class;
            case "int", "int256" -> Int256.class;
            case "int8" -> Int8.class;
            case "int16" -> Int16.class;
            case "int32" -> Int32.class;
            case "int64" -> Int64.class;
            case "int128" -> Int128.class;
            case "bytes32" -> Bytes32.class;
            default -> throw new IllegalArgumentException("Unsupported array element type: " + type);
        };
    }

    private List<String> splitArrayValue(String value) {
        String stripped = value.trim().replaceAll("^\\[|]$", "");
        return Arrays.stream(stripped.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private BigInteger parseBigInt(String value) {
        return new BigInteger(value.trim());
    }

    private byte[] toFixedBytes(String hex, int size) {
        byte[] decoded = Numeric.hexStringToByteArray(normalizeHex(hex));
        byte[] result = new byte[size];
        System.arraycopy(decoded, 0, result, 0, Math.min(decoded.length, size));
        return result;
    }

    private String normalizeHex(String hex) {
        hex = hex.trim();
        return hex.startsWith("0x") || hex.startsWith("0X") ? hex.substring(2) : hex;
    }
}
