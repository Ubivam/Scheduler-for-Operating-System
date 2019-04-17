package com.etf.os2.js150411.scheduler;

import com.etf.os2.js150411.process.Pcb;

public abstract class Scheduler {
    public abstract Pcb get(int cpuId);

    public abstract void put(Pcb pcb);

    public static Scheduler createScheduler(String[] args) {
        Scheduler scheduler = null;
        if (args[0].compareTo("ShortestJobFirst") == 0) {
            float coefficient = Float.parseFloat(args[1]);
            boolean preemptive = Boolean.parseBoolean(args[2]);
            scheduler = new ShortestJobFirst(coefficient, preemptive);
        }
        if (args[0].compareTo("MultilevelFeedbackQueue") == 0) {
            int quantum[]=new int[args.length-1];
            for (int i =0; i<args.length - 2; i++) {
                quantum[i]=Integer.parseInt(args[i+1]);
            }
            scheduler = new MultilevelFeedbackQueue(quantum.length, quantum);
        }
        if(args[0].compareTo("CompletelyFairScheduling")==0){
            scheduler= new CompletelyFairScheduling();
        }
        return scheduler;
    }
}
