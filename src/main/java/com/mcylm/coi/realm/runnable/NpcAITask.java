package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.npc.AI;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class NpcAITask {

    @Getter
    private static Set<AI> aiSet = new HashSet<>();

    public static void runTask(AI ai) {

        if (!aiSet.contains(ai)) {

            aiSet.add(ai);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!aiSet.contains(ai)) {
                        this.cancel();
                        return;
                    }
                    if (ai.isRemoved()) {
                        aiSet.remove(ai);
                        return;
                    }
                    ai.move();

                }
            }.runTaskTimer(Entry.getInstance(), 0, ai.delayTick());
        }
    }
}
