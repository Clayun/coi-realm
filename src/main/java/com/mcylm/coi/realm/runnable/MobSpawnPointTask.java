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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MobSpawnPointTask {

    @Getter
    private static Set<BukkitRunnable> tasks = new HashSet<>();

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
        TeamUtils.getMonsterTeam().addEntityToScoreboard(zombie);
        MobGoals goals = Bukkit.getMobGoals();
        zombie.setMetadata("monsterData", new MonsterData());
        goals.addGoal(zombie,0, new MonsterAttackBuildingGoal(zombie, 8));
        goals.addGoal(zombie,0, new MonsterLookForBuildingTargetGoal(zombie));

    }

    public static void runTask() {
        initHandlers();
        tasks.forEach(BukkitRunnable::cancel);
        tasks.clear();
        for (COIMobSpawnPoint point : Entry.getMapData().getMobSpawnPoints()) {
            point.setHandlers(new ArrayList<>(defaultSpawnHandlers));
            tasks.add(point.startSpawn());
        }
    }

}
