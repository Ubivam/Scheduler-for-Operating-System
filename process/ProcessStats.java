package com.etf.os2.js150411.process;

import java.util.List;

public class ProcessStats {
    private long responseTime = 0;
    private long executionTime = 0;
    private final long expectedExecutionTime;
    private long putTime = 0;

    public ProcessStats(List<Process.Request> requests) {
        long calcTime = 0;
        for (Process.Request req : requests) {
            if (req.state == Pcb.ProcessState.RUNNING) {
                calcTime += req.time;
            }
        }
        expectedExecutionTime = calcTime;
    }

    public void addExecutionTime(long time) {
        executionTime += time;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void addProcessToScheduler(long time) {
        putTime = time;
    }

    public void processLostCpu(long time) {
        responseTime += time - putTime;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public long getExpectedExecutionTime() {
        return expectedExecutionTime;
    }
}
