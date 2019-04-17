package com.etf.os2.js150411.process;

import com.etf.os2.js150411.scheduler.Scheduler;

import java.util.*;

public class Process {
    private final static Random GENERATOR = new Random(0);
    private final static long MAX_LENGTH = 1000;
    private final static long MIN_LENGTH = 100;
    private final static long MAX_BURST_LENGTH = 100;
    private final static long MIN_BURST_LENGTH = 5;
    private final static long MAX_ACTIVATION_TIME = 20;
    private static long systemTime = 0;
    private static int processCount = 0;

    public enum ProcessType {CPU_BOUND, IO_BOUND, AVERAGE}

    public enum ProcessLength {SHORT, MEDIUM, LONG}

    public static class Request {
        public final Pcb.ProcessState state;
        public long time;

        public Request(Pcb.ProcessState state, long time) {
            this.state = state;
            this.time = time;
        }
    }

    private final static Map<Pcb, Process> PROCESS = new HashMap<Pcb, Process>();

    public static Process getProcess(Pcb pcb) {
        return PROCESS.get(pcb);
    }

    private final Pcb pcb;
    private long activationTime;
    private long length;
    private long time = 0;
    private final List<Request> requests = new ArrayList<Request>();
    private Request currentRequest = null;
    private Pcb.ProcessState state = Pcb.ProcessState.CREATED;
    private ProcessStats stats;

    public Process(int id, int priority, ProcessType processType, ProcessLength processLength) {
        pcb = new Pcb(id, priority);

        PROCESS.put(pcb, this);

        generateLength(processLength);

        generateRequests(processType);

        activationTime = (long) (GENERATOR.nextDouble() * (MAX_ACTIVATION_TIME + 1));

        System.out.println(toString());

        if (pcb != Pcb.IDLE) {
            processCount++;
        }

        stats = new ProcessStats(requests);
    }

    public String toString() {
        StringBuilder ret = new StringBuilder(
                String.format("Process id=%d priority=%d StartTime=%d ", pcb.getId(), pcb.getPriority(), activationTime));
        for (Request req : requests) {
            if (req.state == Pcb.ProcessState.RUNNING) {
                ret.append("C");
            } else {
                ret.append("IO");
            }
            ret.append(req.time + " ");
        }

        return ret.toString();
    }

    public Process() {
        pcb = Pcb.IDLE;
        PROCESS.put(pcb, this);
    }

    private void generateRequests(ProcessType processType) {
        RandomGenerator cpuGenerator = null;
        RandomGenerator ioGenerator = null;
        switch (processType) {
            case CPU_BOUND:
                ioGenerator = new RandomGenerator(GENERATOR,
                        new int[]{9, 7, 4, 3, 2, 1}, MAX_BURST_LENGTH, MIN_BURST_LENGTH);
                cpuGenerator = new RandomGenerator(GENERATOR,
                        new int[]{0, 1, 3, 4, 7, 9}, MAX_BURST_LENGTH, MIN_BURST_LENGTH);
                break;
            case AVERAGE:
                cpuGenerator = ioGenerator = new RandomGenerator(GENERATOR,
                        new int[]{0, 1, 3, 4, 7, 9, 7, 4, 3, 2, 1}, MAX_BURST_LENGTH, MIN_BURST_LENGTH);
                break;
            case IO_BOUND:
                cpuGenerator = new RandomGenerator(GENERATOR,
                        new int[]{9, 7, 4, 3, 2, 1}, MAX_BURST_LENGTH, MIN_BURST_LENGTH);
                ioGenerator = new RandomGenerator(GENERATOR,
                        new int[]{0, 1, 3, 4, 7, 9}, MAX_BURST_LENGTH, MIN_BURST_LENGTH);
                break;
        }

        long currentLength = 0;
        int i = 0;
        while (currentLength < length) {
            long runTime;
            Pcb.ProcessState state;
            if (i % 2 == 0) {
                runTime = cpuGenerator.getNext();
                state = Pcb.ProcessState.RUNNING;
            } else {
                runTime = ioGenerator.getNext();
                state = Pcb.ProcessState.BLOCKED;
            }

            requests.add(new Request(state, runTime));
            currentLength += runTime;
            i++;
        }

        length = currentLength;
    }

    private void generateLength(ProcessLength processLength) {
        RandomGenerator lengthGenerator = null;
        switch (processLength) {
            case SHORT:
                lengthGenerator = new RandomGenerator(GENERATOR,
                        new int[]{9, 7, 4, 3, 2, 1}, MAX_LENGTH, MIN_LENGTH);
                break;
            case MEDIUM:
                lengthGenerator = new RandomGenerator(GENERATOR,
                        new int[]{0, 1, 3, 4, 7, 9, 7, 4, 3, 2, 1}, MAX_LENGTH, MIN_LENGTH);
                break;
            case LONG:
                lengthGenerator = new RandomGenerator(GENERATOR,
                        new int[]{0, 1, 3, 4, 7, 9}, MAX_LENGTH, MIN_LENGTH);
                break;
        }

        length = lengthGenerator.getNext();
    }


    public long getNextEventTime() {
        if (isPreempt()) {
            return time + 1;
        }
        return activationTime;
    }

    public void step(Scheduler scheduler, long currentTime) {
        systemTime = currentTime;
        switch (state) {
            case CREATED:
                if (currentTime >= activationTime) {
                    pcb.setPreviousState(Pcb.ProcessState.CREATED);
                    state = Pcb.ProcessState.READY;
                    scheduler.put(pcb);
                    stats.addProcessToScheduler(getCurrentTime());
                }
                break;
            case READY:
            case RUNNING:
            case IDLE:
                break;
            case BLOCKED:
                if (currentTime >= activationTime) {
                    System.out.printf("IO PID%d TIME%d\n", pcb.getId(), currentRequest.time);
                    pcb.setPreviousState(Pcb.ProcessState.BLOCKED);
                    state = Pcb.ProcessState.READY;
                    if (requests.size() > 0) {
                        currentRequest = requests.remove(0);
                        assert currentRequest.state == Pcb.ProcessState.RUNNING;
                        state = Pcb.ProcessState.READY;
                        scheduler.put(pcb);
                        stats.addProcessToScheduler(getCurrentTime());
                    } else {
                        state = Pcb.ProcessState.FINISHED;
                    }
                }
                break;
        }

        time = currentTime;
    }

    public boolean isPreempt() {
        return pcb.isPreempt();
    }

    public boolean isIdle() {
        return pcb == Pcb.IDLE;
    }

    public boolean isFinished() {
        return state == Pcb.ProcessState.FINISHED;
    }

    public void saveContext(Scheduler scheduler, int cpuId, long timeExecuted) {
        if (pcb.getAffinity() != cpuId) {
            long cacheMissTime = Math.round(timeExecuted * (GENERATOR.nextDouble() / 5));
            if (cacheMissTime < 1) {
                cacheMissTime = 1;
            }
            currentRequest.time += cacheMissTime;
        }

        stats.processLostCpu(getCurrentTime());
        pcb.setPreviousState(Pcb.ProcessState.RUNNING);
        pcb.setExecutionTime(timeExecuted);
        pcb.setAffinity(cpuId);
        stats.addExecutionTime(timeExecuted);

        currentRequest.time -= timeExecuted;

        if (currentRequest.time > 0) {
            state = Pcb.ProcessState.READY;
            scheduler.put(pcb);
            stats.addProcessToScheduler(getCurrentTime());
        } else {
            if (requests.size() > 0) {
                currentRequest = requests.remove(0);
                assert currentRequest.state == Pcb.ProcessState.BLOCKED;

                activationTime = time + currentRequest.time;
                state = Pcb.ProcessState.BLOCKED;
            } else {
                state = Pcb.ProcessState.FINISHED;
            }
        }
    }

    public long restoreContext(int cpuId) {
        Pcb.RUNNING[cpuId] = pcb;
        state = Pcb.ProcessState.RUNNING;

        if (isIdle()) {
            return Long.MAX_VALUE;
        }

        if (currentRequest == null) {
            currentRequest = requests.remove(0);
        }

        assert currentRequest.state == Pcb.ProcessState.RUNNING;

        long executionTime = (currentRequest.time > pcb.getTimeslice() && pcb.getTimeslice() != 0) ?
                pcb.getTimeslice() : currentRequest.time;
        assert (executionTime > 0);

        activationTime = Long.MAX_VALUE;

        return executionTime;
    }

    public void preempt() {
        pcb.setPreempt(false);
    }

    public static void removeProcess() {
        processCount--;
    }

    public static long getCurrentTime() {
        return systemTime;
    }

    public static int getProcessCount() {
        return processCount;
    }

    public void writeResults() {
        StringBuilder ret = new StringBuilder(
                String.format("Process id=%d priority=%d ", pcb.getId(), pcb.getPriority()));
        ret.append(String.format("ExpectedExecutionTime=%d ExecutionTime=%d ", stats.getExpectedExecutionTime(),
                stats.getExecutionTime()));
        ret.append(String.format("ResponseTime=%d ", stats.getResponseTime()));

        System.out.println(ret);
    }
}
