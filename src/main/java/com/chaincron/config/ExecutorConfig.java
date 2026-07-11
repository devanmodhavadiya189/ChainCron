package com.chaincron.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ExecutorConfig {

    private final AppProperties appProperties;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService jobWorkerPool() {
        int poolSize = appProperties.getScheduler().getExecutorPoolSize();
        log.info("Creating job worker thread pool with {} threads", poolSize);
        return Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r, "job-worker-" + System.nanoTime());
            t.setDaemon(false);
            return t;
        });
    }
}
