package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.attack.AttackGoal;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class AttackGoalTask {

    @Getter
    private static HashSet<AttackGoal> goalSet = new HashSet<>();

    public static void runTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                new HashSet<>(goalSet).forEach(goal -> {
                    if (!goal.isStop()) {
                        if (goal.getExecutor() instanceof COIEntity entity) {
                            if (!entity.isTooHungryToWork()) {
                                goal.tick();
                            }
                        }
                    }
                });
            }
        }.runTaskTimer(Entry.getInstance(), 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                new HashSet<>(goalSet).forEach(goal -> {
                if (!goal.isStop()) {
                    if (goal.getExecutor() instanceof COIEntity entity) {
                        if (!entity.isTooHungryToWork()) {
                            goal.asyncTick();
                        }
                    }
                }
                });
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 1, 1);


    }

}
