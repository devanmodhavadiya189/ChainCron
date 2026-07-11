package com.chaincron.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "chaincron")
public class AppProperties {

    @Valid
    private Ethereum ethereum = new Ethereum();

    @Valid
    private Platform platform = new Platform();

    @Valid
    private Frontend frontend = new Frontend();

    @Valid
    private Alchemy alchemy = new Alchemy();

    @Valid
    private Scheduler scheduler = new Scheduler();

    @Valid
    private Kafka kafka = new Kafka();

    @Getter
    @Setter
    public static class Ethereum {
        @NotBlank
        private String rpcUrl;
        @NotBlank
        private String platformWalletAddress;
        @NotBlank
        private String platformWalletPrivateKey;
        @Positive
        private long chainId = 11155111L;
    }

    @Getter
    @Setter
    public static class Platform {
        @NotNull
        @Positive
        private BigDecimal feeWei = new BigDecimal("1000000000000000");
    }

    @Getter
    @Setter
    public static class Frontend {
        @NotBlank
        private String redirectUrl = "http://localhost:3000/auth/callback";
    }

    @Getter
    @Setter
    public static class Alchemy {
        @NotBlank
        private String webhookAuthToken;
    }

    @Getter
    @Setter
    public static class Scheduler {
        @Positive
        private int pollIntervalSeconds = 15;
        @Positive
        private int executorPoolSize = 8;
        @Positive
        private int confirmationCheckDelaySeconds = 20;
        @Positive
        private int staleJobTimeoutSeconds = 120;
        @Positive
        private int staleJobSweepIntervalSeconds = 90;
    }

    @Getter
    @Setter
    public static class Kafka {
        private Topic topic = new Topic();

        @Getter
        @Setter
        public static class Topic {
            private String jobExecutions = "job-executions";
        }
    }
}
