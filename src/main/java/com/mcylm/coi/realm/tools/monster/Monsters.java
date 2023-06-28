package com.mcylm.coi.realm.tools.monster;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.data.metadata.MonsterData;
import com.mcylm.coi.realm.tools.goals.paper.MonsterAttackBuildingGoal;
import com.mcylm.coi.realm.tools.goals.paper.MonsterLookForBuildingTargetGoal;
import com.mcylm.coi.realm.tools.map.COIMobSpawnPoint;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

public class Monsters {

    public static void spawnAll(int round) {
        for (COIMobSpawnPoint point : Entry.getMapData().getMobSpawnPoints()) {
            spawnZombie(point.getLocation(), round);
        }
    }



    public static void spawnZombie(Location location, int round) {
        Zombie zombie = location.getWorld().spawn(location, Zombie.class);
        configureMonsterGoalsAndBehaviors(zombie);
    }

    public static void configureMonsterGoalsAndBehaviors(Monster monster) {
        if (monster instanceof Zombie zombie) {
            zombie.setShouldBurnInDay(false);
        }

        if (monster instanceof Skeleton skeleton) {
            skeleton.setShouldBurnInDay(false);
        }

        monster.setRemoveWhenFarAway(false);

        TeamUtils.getMonsterTeam().addEntityToScoreboard(monster);
        MobGoals goals = Bukkit.getMobGoals();
        monster.setMetadata("monsterData", new MonsterData());
        goals.addGoal(monster,0, new MonsterAttackBuildingGoal(monster, 8));

        new MonsterLookForBuildingTargetGoal(monster);

    }

}
