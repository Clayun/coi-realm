package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.map.COIVein;

import java.security.SecureRandom;

public class VeinGenerateTask {
    public static void runTask() {
        SecureRandom random = new SecureRandom();
        for (COIVein vein : Entry.getMapData().getVeins()) {
            if (random.nextDouble() < vein.getSpawnChance()) {
                vein.startGenerate();
            }
        }
    }
}
