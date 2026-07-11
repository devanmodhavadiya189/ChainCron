package com.chaincron.util.abi;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.utils.Numeric;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CalldataEncoder {

    private final AbiParser abiParser;
    private final Web3jTypeConverter typeConverter;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public byte[] encode(String abiJson, String functionName, List<String> paramValues) {
        AbiItem function = abiParser.findFunction(abiJson, functionName);
        List<AbiInputParam> inputs = function.getInputs() == null
                ? Collections.emptyList()
                : function.getInputs();

        if (inputs.size() != paramValues.size()) {
            throw new IllegalArgumentException(
                    "Parameter count mismatch: ABI expects " + inputs.size()
                    + " but received " + paramValues.size());
        }

        List encodedInputs = buildEncodedInputs(inputs, paramValues);
        Function web3jFunction = new Function(functionName, encodedInputs, Collections.emptyList());

        String hexCalldata = FunctionEncoder.encode(web3jFunction);
        return Numeric.hexStringToByteArray(hexCalldata);
    }

    public String buildSignature(String functionName, List<AbiInputParam> inputs) {
        String paramTypes = inputs.stream()
                .map(AbiInputParam::getType)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        return functionName + "(" + paramTypes + ")";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List buildEncodedInputs(List<AbiInputParam> inputs, List<String> values) {
        return java.util.stream.IntStream.range(0, inputs.size())
                .mapToObj(i -> typeConverter.convert(inputs.get(i).getType(), values.get(i)))
                .toList();
    }
}
