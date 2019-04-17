package com.etf.os2.js150411.scheduler;

import com.etf.os2.js150411.process.Pcb;
import com.etf.os2.js150411.process.PcbData;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ShortestJobFirst extends Scheduler {

    private List<Pcb> PCBList;
    private float coefficient;
    private boolean preemptive;

    Semaphore mutex = new Semaphore(1);

    public ShortestJobFirst(float coefficient, boolean preemptive) {
        PCBList = new LinkedList<Pcb>();
        if (coefficient >= 0 && coefficient <= 1) {
            this.coefficient = coefficient;
        }
        this.preemptive = preemptive;
    }

    @Override
    public Pcb get(int cpuId) {
        try {
            mutex.acquire();

            Pcb newPcb = null;
            float averageWeightedTime = 0;
            if (PCBList.size() == 0) {return null;}
            if(PCBList.get(0).getPcbData().getCpuID() == -1){
                newPcb = PCBList.remove(0);
                newPcb.getPcbData().setCpuID(cpuId);
                return newPcb;
            }
            for (int i = 0; i < PCBList.size(); i++) {
                averageWeightedTime = (averageWeightedTime + PCBList.get(i).getPcbData().getWeightedTime());
                if ((averageWeightedTime / i) < (PCBList.get(i).getPcbData().getWeightedTime() * coefficient)) {
                    newPcb = PCBList.remove(0);
                    newPcb.getPcbData().setCpuID(cpuId);
                    return newPcb;
                }
                if (PCBList.get(i).getPcbData().getCpuID() == cpuId) {
                    newPcb = PCBList.remove(i);
                    return newPcb;
                }
            }
            if (PCBList.size() != 0) {
                newPcb = PCBList.remove(0);
            }
            if (newPcb != null) {
                newPcb.getPcbData().setCpuID(cpuId);
                return newPcb;
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
                pcb.setTimeslice(0);
                pd = pcb.getPcbData();
                pd.setWeightedTime(0);
                pd.setCpuID(-1);
            }
            long newWeightedTime = (long) (pd.getWeightedTime() * coefficient + (1 - coefficient) * pcb.getExecutionTime());
            pd.setWeightedTime(newWeightedTime);
            pd.setLastExecutionTime(pcb.getExecutionTime());
            if (preemptive) {
                for (int i = 0; i < Pcb.RUNNING.length; i++) {
                    long remaingingTime;
                    if (Pcb.RUNNING[i].getPcbData() != null) {
                        remaingingTime = Pcb.RUNNING[i].getPcbData().getLastExecutionTime() - Pcb.RUNNING[i].getExecutionTime();

                        if (remaingingTime > newWeightedTime) {
                            Pcb.RUNNING[i].preempt();
                        }
                    }
                }
            }
            if (PCBList.size() == 0) {
                PCBList.add(pcb);
            }
            for (int i = 0; i < PCBList.size(); i++) {
                long iteratedWeigtedTime = PCBList.get(i).getPcbData().getWeightedTime();
                if (pd.getWeightedTime() < iteratedWeigtedTime) {
                    PCBList.add(i, pcb);
                    return;
                }
                if (pd.getWeightedTime() == iteratedWeigtedTime) {
                    if (pcb.getPriority() < PCBList.get(i).getPriority()) {
                        PCBList.add(i, pcb);
                        return;
                    }
                }
            }
            PCBList.add(pcb);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
    }
}
