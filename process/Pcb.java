package com.etf.os2.js150411.process;

public class Pcb {
    public static Pcb[] RUNNING;
    public static Pcb IDLE;

    static {
        IDLE = new Pcb(0, Integer.MAX_VALUE);
        IDLE.setPreviousState(ProcessState.IDLE);
    }

    private final int id;
    private final int priority;
    private int affinity;
    private long timeslice;
    private long executionTime = 0;
    private ProcessState previousState = ProcessState.CREATED;
    private PcbData pcbData;
    private boolean preempt = false;

    public enum ProcessState { RUNNING, READY, BLOCKED, CREATED, IDLE, FINISHED; }

    public Pcb(int id, int priority) {
        this.id = id;
        this.priority = priority;
    }

    public void preempt() {
        preempt = true;
    }

    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public long getTimeslice() {
        return timeslice;
    }

    public void setTimeslice(long timeslice) {
        this.timeslice = timeslice;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public ProcessState getPreviousState() {
        return previousState;
    }

    public void setPreviousState(ProcessState previousState) {
        this.previousState = previousState;
    }

    public PcbData getPcbData() {
        return pcbData;
    }

    public void setPcbData(PcbData pcbData) {
        this.pcbData = pcbData;
    }

    public boolean isPreempt() {
        return preempt;
    }

    public void setPreempt(boolean preempt) {
        this.preempt = preempt;
    }

    public int getAffinity() {
        return affinity;
    }

    public void setAffinity(int affinity) {
        this.affinity = affinity;
    }

    public static long getCurrentTime() {
        return Process.getCurrentTime();
    }

    public static int getProcessCount() {
        return Process.getProcessCount();
    }
}
