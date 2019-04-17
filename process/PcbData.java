package com.etf.os2.js150411.process;

public class PcbData {
    private int cpuID;
	//Shortest Job First
    private long weightedTime;
    //MLFQ
    private long lastExecutionTime;
    private int lastPriority;
    //CFS
    private long virtualime;
    private long queueTime;

    public long getWeightedTime() {
        return weightedTime;
    }

    public void setWeightedTime(long weightedTime) {
        this.weightedTime = weightedTime;
    }

    public int getCpuID() {
        return cpuID;
    }

    public void setCpuID(int cpuID) {
        this.cpuID = cpuID;
    }

    public long getLastExecutionTime() {
        return lastExecutionTime;
    }

    public void setLastExecutionTime(long lastExecutionTime) {
        this.lastExecutionTime = lastExecutionTime;
    }

    public int getLastPriority() {
        return lastPriority;
    }

    public void setLastPriority(int lastPriority) {
        this.lastPriority = lastPriority;
    }

    public long getVirtualime() {
        return virtualime;
    }

    public void setVirtualime(long virtualime) {
        this.virtualime = virtualime;
    }

    public long getQueueTime() {
        return queueTime;
    }

    public void setQueueTime(long queueTime) {
        this.queueTime = queueTime;
    }
}
