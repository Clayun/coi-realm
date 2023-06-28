package com.mcylm.coi.realm.runnable;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.data.metadata.MonsterData;
import com.mcylm.coi.realm.tools.goals.paper.MonsterAttackBuildingGoal;
import com.mcylm.coi.realm.tools.goals.paper.MonsterLookForBuildingTargetGoal;
import com.mcylm.coi.realm.tools.handler.SpawnHandler;
import com.mcylm.coi.realm.tools.map.COIMobSpawnPoint;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class MobSpawnPointTask {

    private static BukkitRunnable task;

    @Getter
    private static List<SpawnHandler> defaultSpawnHandlers;

    public static void initHandlers() {

        defaultSpawnHandlers = new ArrayList<>();
        defaultSpawnHandlers.add(new SpawnHandler() {
            int maxMobs = 8;

            int spawnedMobs = 0;
            @Override
            public void spawn(int second, Location location) {

                if (second % 120 == 0) {
                    spawnedMobs = 0;
                }

                if (second % 2 == 0) {
                    if (spawnedMobs < maxMobs) {
                        spawnedMobs++;
                        spawnZombie(location);
                    }
                }
            }
        });
    }

    private static void spawnZombie(Location location) {
        Zombie zombie = location.getWorld().spawn(location, Zombie.class);
        zombie.setShouldBurnInDay(false);
        zombie.setRemoveWhenFarAway(false);

        TeamUtils.getMonsterTeam().addEntityToScoreboard(zombie);
        MobGoals goals = Bukkit.getMobGoals();
        zombie.setMetadata("monsterData", new MonsterData());
        goals.addGoal(zombie,0, new MonsterAttackBuildingGoal(zombie, 8));

        new MonsterLookForBuildingTargetGoal(zombie);


    }

    public static void runTask() {
        initHandlers();
        if (task != null) {
            task.cancel();
        }

        for (COIMobSpawnPoint point : Entry.getMapData().getMobSpawnPoints()) {
            point.setHandlers(new ArrayList<>(defaultSpawnHandlers));
        }
        task = new BukkitRunnable() {
            int second = 0;
            @Override
            public void run() {
                for (COIMobSpawnPoint point : Entry.getMapData().getMobSpawnPoints()) {
                    point.spawn(second);
                }
                second++;
            }
        };
        task.runTaskTimer(Entry.getInstance(), 0, 20);

    }

}
