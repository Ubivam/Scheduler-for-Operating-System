package com.etf.os2.js150411.scheduler;

import com.etf.os2.js150411.process.Pcb;
import com.etf.os2.js150411.process.PcbData;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class CompletelyFairScheduling extends Scheduler {
    Semaphore mutex;
    List<Pcb> PCBList;

    CompletelyFairScheduling() {
        mutex = new Semaphore(1);
        PCBList = new LinkedList<Pcb>();
    }

    @Override
    public Pcb get(int cpuId) {
        try {
            mutex.acquire();
            if (PCBList.size() != 0) {
                long virtualTime = 0;
                Pcb pcb = null;
                boolean ok =false;
                if (PCBList.get(0).getPcbData().getCpuID() == -1) {
                    ok=true;
                    pcb = PCBList.remove(0);
                }
                for (int i = 0; i < PCBList.size() - 1; i++) {
                    virtualTime+=PCBList.get(i).getPcbData().getVirtualime();
                    if (PCBList.get(i).getPcbData().getCpuID() == cpuId) {
                        ok=true;
                        pcb = PCBList.remove(i);
                        break;
                    }
                }
                if(!ok){
                    PCBList.remove(0);
                }
                long timeSlice = Pcb.getCurrentTime() - pcb.getPcbData().getQueueTime();
                pcb.getPcbData().setQueueTime(0);
                pcb.setTimeslice(timeSlice);
                return pcb;
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
                pd.setVirtualime(0);
            }
            if (pcb.getPreviousState() != Pcb.ProcessState.RUNNING) {
                pd.setVirtualime(0);
            } else {
                long virtualTime = pd.getVirtualime() + pcb.getExecutionTime();
                pd.setVirtualime(virtualTime);
            }
            pd.setQueueTime(Pcb.getCurrentTime());
            boolean ok = false;
            for (int i = 0; i < PCBList.size() - 1; i++) {
                if (PCBList.get(i).getPcbData().getVirtualime() >= pd.getVirtualime()) {
                    ok = true;
                    PCBList.add(i, pcb);
                    break;
                }
            }
            if (!ok) {
                PCBList.add(pcb);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
    }
}