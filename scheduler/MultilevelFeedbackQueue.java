package com.etf.os2.js150411.scheduler;

import com.etf.os2.js150411.process.Pcb;
import com.etf.os2.js150411.process.PcbData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class MultilevelFeedbackQueue extends Scheduler {
    List<List<Pcb>> PCBLists;
    Semaphore mutex;
    int quantum[];

    MultilevelFeedbackQueue(int numberOfLists, int quantum[]) {
        PCBLists = new ArrayList<List<Pcb>>(numberOfLists - 1);
        for (int i = 0; i < numberOfLists; i++) {
            PCBLists.add(new LinkedList<Pcb>());
        }
        this.quantum = quantum;
        mutex = new Semaphore(1);
    }

    @Override
    public Pcb get(int cpuId) {
        try {
            mutex.acquire();
            for (int i = 0; i < PCBLists.size(); i++) {
                if (PCBLists.get(i).size() != 0) {
                    Pcb pcb = PCBLists.get(i).get(0);
                    boolean ok = false;
                    for (int j = 0; PCBLists.get(i).size() > j; j++) {
                        if(pcb.getPcbData().getCpuID() == -1){
                            ok=true;
                            pcb = PCBLists.get(i).remove(j);
                            break;
                        }
                        if(pcb.getPcbData().getCpuID() == cpuId){
                            ok=true;
                            pcb = PCBLists.get(i).remove(j);
                            break;
                        }
                    }
                    if(!ok){
                        pcb = PCBLists.get(i).remove(0);
                    }
                    pcb.setTimeslice(quantum[i]);
                    pcb.getPcbData().setCpuID(cpuId);
                    return pcb;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
        return null;
    }

    @Override
    public void put(Pcb pcb) {
        try {
            mutex.acquire();
            PcbData pd = pcb.getPcbData();
            if (pd == null) {
                pcb.setPcbData(new PcbData());
                pd = pcb.getPcbData();
                pd.setCpuID(-1);
                int priority = pcb.getPriority();
                if(priority>PCBLists.size()){
                    priority=PCBLists.size();
                }
                pd.setLastPriority(priority);
                if (priority < PCBLists.size() - 1) {
                    PCBLists.get(priority).add(pcb);
                } else {
                    PCBLists.get(PCBLists.size() - 1).add(pcb);
                }
                return;
            }
            if (pcb.getPreviousState() == Pcb.ProcessState.BLOCKED) {
                if (pd != null) {
                    if (pd.getLastPriority() > 0) {
                        pd.setLastPriority(pd.getLastPriority() - 1);
                    }
                    int priority = pd.getLastPriority();
                    if (priority < PCBLists.size() - 1) {
                        PCBLists.get(priority).add(pcb);
                    } else {
                        PCBLists.get(PCBLists.size() - 1).add(pcb);
                    }
                    return;
                }
            }
            if (pd.getLastPriority() < PCBLists.size()-1) {
                pd.setLastPriority(pd.getLastPriority() + 1);
            }
            int priority = pd.getLastPriority();
            if (priority < PCBLists.size() - 1) {
                PCBLists.get(priority).add(pcb);
            } else {
                PCBLists.get(PCBLists.size() - 1).add(pcb);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
    }
}
