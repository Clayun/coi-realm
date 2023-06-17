package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.attack.AttackGoal;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;

public class AttackGoalTask {

    @Getter
    private static HashSet<AttackGoal> goalSet = new HashSet<>();

    public static void runTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<AttackGoal> iterator = goalSet.iterator();
                if (iterator.hasNext()) {
                    do {
                        AttackGoal goal = iterator.next();
                        if (!goal.isStop()) {
                            if (goal.getExecutor() instanceof COIEntity entity) {
                                if (!entity.isTooHungryToWork()) {
                                    goal.tick();
                                }
                            }
                        }

                    } while (iterator.hasNext());
                }
            }
        }.runTaskTimer(Entry.getInstance(), 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<AttackGoal> iterator = goalSet.iterator();
                if (iterator.hasNext()) {
                    do {
                        AttackGoal goal = iterator.next();
                        if (!goal.isStop()) {
                            if (goal.getExecutor() instanceof COIEntity entity) {
                                if (!entity.isTooHungryToWork()) {
                                    goal.asyncTick();
                                }
                            }
                        }

                    } while (iterator.hasNext());

                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 1, 1);


    }

}
