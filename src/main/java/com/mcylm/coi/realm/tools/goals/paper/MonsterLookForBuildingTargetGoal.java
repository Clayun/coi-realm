package com.mcylm.coi.realm.tools.goals.paper;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.tools.attack.target.impl.BuildingTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.MonsterData;
import com.mcylm.coi.realm.utils.LocationUtils;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class MonsterLookForBuildingTargetGoal implements Goal<Monster> {

    private Monster monster;

    private int tick = 0;

    private CompletableFuture<BuildingTarget> targetFuture = null;


    public MonsterLookForBuildingTargetGoal(Monster monster) {
        this.monster = monster;
        // 为了不干扰原版 使用BukkitRunnable
        MonsterLookForBuildingTargetGoal goal = this;
        new BukkitRunnable() {
            @Override
            public void run() {

                if (monster.isDead() || Bukkit.isStopping()) {
                    this.cancel();
                    return;
                }


                MonsterData data = MonsterData.getDataByEntity(monster);

                if (monster.getTarget() == null || monster.getTarget().isDead()) {
                    if (data.getTarget() != null && !data.getTarget().isDead()) {
                        Entry.runSync(() -> {
                            if (monster.getPathfinder().getCurrentPath() != null && monster.getPathfinder().getCurrentPath().getFinalPoint().distance(data.getTarget().getTargetLocation()) < 2) {
                                return;
                            }
                            monster.getPathfinder().moveTo(data.getTarget().getTargetLocation());
                        });
                    }
                }

                if (data.getTarget() != null && data.getTarget().isDead()) {
                    data.setTarget(null);
                }

                if (goal.shouldActivate()) {
                    goal.tick();
                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 1,10);

    }

    @Override
    public boolean shouldActivate() {

        if (monster.getTarget() != null && !monster.getTarget().isDead()) {
            if (monster.getTarget().getLocation().distance(monster.getLocation()) < 10) {
                return false;
            }
        }
        MonsterData data = MonsterData.getDataByEntity(monster);
        if (data != null && data.getTarget() != null && !data.getTarget().isDead()) {
            return false;
        }

        return true;
    }

    @Override
    public void tick() {

        monster.setTarget(null);
        MonsterData data = MonsterData.getDataByEntity(monster);

        for (COIBuilding building : LocationUtils.selectionBuildingsByDistance(monster.getLocation(), 30, EnumSet.of(COITeamType.MONSTER), true)) {
            if (building.getType() != COIBuildingType.WALL_NORMAL) {
                data.setTarget(new BuildingTarget(building, building.getNearestBlock(monster.getLocation()).getLocation(), 6));
            }
        }



    }


    @Override
    public @NotNull GoalKey<Monster> getKey() {
        return GoalKey.of(Monster.class, Entry.getNamespacedKey("look_for_target"));
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.UNKNOWN_BEHAVIOR);
    }

}
