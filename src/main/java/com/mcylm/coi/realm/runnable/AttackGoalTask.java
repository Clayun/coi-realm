package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.attack.AttackGoal;
import com.mcylm.coi.realm.tools.npc.AI;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import javax.xml.stream.events.EntityReference;
import java.util.HashSet;
import java.util.Set;

public class AttackGoalTask {

    @Getter
    private static Set<AttackGoal> goalSet = new HashSet<>();

    public static void runTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                goalSet.forEach(goal -> {
                    if (!goal.isStop()) {
                        goal.tick();
                    }
                });
            }
        }.runTaskTimer(Entry.getInstance(), 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                goalSet.forEach(goal -> {
                if (!goal.isStop()) {
                    goal.asyncTick();
                }
                });
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 1, 1);


    }

}
