package com.etf.os2.js150411.system;

import com.etf.os2.js150411.process.Pcb;
import com.etf.os2.js150411.process.Process;
import com.etf.os2.js150411.scheduler.Scheduler;

public class Cpu {
    private final int id;
    private Process process;
    private long time;
    private long activationTime = 0;
    private long lastActivationTime = 0;

    public Cpu(int id) {
        this.id = id;

        process = Process.getProcess(Pcb.IDLE);
        if (process == null) {
            process = new Process();
        }
    }

    public long getNextEventTime() {
        return activationTime;
    }

    public void step(Scheduler scheduler, long currentTime) {
        if (currentTime >= activationTime || process.isIdle()) {
            changeContext(scheduler, currentTime);
        }

        time = currentTime;
    }

    private void changeContext(Scheduler scheduler, long currentTime) {
        if (!process.isIdle()) {
            java.lang.System.out.printf("CPU%d PID%d TIME%d\n",
                    id, Pcb.RUNNING[id].getId(), currentTime - lastActivationTime);
            process.saveContext(scheduler, id, currentTime - lastActivationTime);
        }

        Pcb pcb = scheduler.get(id);
        if (pcb == null) {
            pcb = Pcb.IDLE;
        }

        process = Process.getProcess(pcb);
        if (process.isIdle()) {
            process.restoreContext(id);
            activationTime = Long.MAX_VALUE;
        } else {
            activationTime = currentTime + process.restoreContext(id);
        }
        lastActivationTime = currentTime;
    }

    public boolean tryToPreempt(Scheduler scheduler) {
        if (process.isPreempt()) {
            process.preempt();

            changeContext(scheduler, time);

            return true;
        }

        return false;
    }
}
