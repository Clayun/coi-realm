package com.mcylm.coi.realm.runnable;

import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.BlockBreaker;
import org.bukkit.Bukkit;

public class TaskRunnable implements Runnable{

    private int taskId;
    private final BlockBreaker breaker;

    public TaskRunnable(BlockBreaker breaker) {
        this.breaker = breaker;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public void run() {
        if (breaker.run() != BehaviorStatus.RUNNING) {
            Bukkit.getScheduler().cancelTask(taskId);
            breaker.reset();
        }
    }
}