package com.chaincron.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Web3jConfig {

    private final AppProperties appProperties;

    @Bean
    public Web3j web3j() {
        String rpcUrl = appProperties.getEthereum().getRpcUrl();
        log.info("Connecting Web3j to RPC endpoint: {}", rpcUrl);
        Web3j instance = Web3j.build(new HttpService(rpcUrl));
        log.info("Web3j connected to Sepolia chain-id={}", appProperties.getEthereum().getChainId());
        return instance;
    }
}
