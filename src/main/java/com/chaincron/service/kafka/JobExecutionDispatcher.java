package com.chaincron.service.kafka;

public interface JobExecutionDispatcher {

    void dispatch(Long jobId);
}
