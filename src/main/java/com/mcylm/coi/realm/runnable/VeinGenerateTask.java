package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.map.COIVein;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

public class VeinGenerateTask {
    @Getter
    private static Set<BukkitRunnable> tasks = new HashSet<>();

    public static void runTask() {
        SecureRandom random = new SecureRandom();
        for (COIVein vein : Entry.getMapData().getVeins()) {
            if (random.nextDouble() < vein.getSpawnChance()) {
                tasks.add(vein.startGenerate());
            }
        }
    }
}
