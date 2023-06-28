package com.mcylm.coi.realm.tools.goals.paper;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.attack.target.impl.BuildingTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.tools.data.metadata.MonsterData;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.AllArgsConstructor;
import org.bukkit.block.Block;
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

                if (monster.isDead()) {
                    this.cancel();
                    return;
                }


                MonsterData data = MonsterData.getDataByEntity(monster);

                if (monster.getTarget() == null || monster.getTarget().isDead()) {

                    if (data.getTarget() != null && !data.getTarget().isDead()) {
                        Entry.runSync(() -> monster.getPathfinder().findPath(data.getTarget().getTargetLocation()));
                    }
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
            return false;
        }
        LoggerUtils.debug("yeah no target");
        MonsterData data = MonsterData.getDataByEntity(monster);
        if (data != null && data.getTarget() != null && !data.getTarget().isDead()) {
            return false;
        }
        LoggerUtils.debug("yeah no building target");

        return true;
    }

    @Override
    public void tick() {

        MonsterData data = MonsterData.getDataByEntity(monster);

       for (Block b : LocationUtils.selectionRadiusByDistance(monster.getLocation().getBlock(), 32, 10)) {
           COIBuilding building = BuildData.getBuildingByBlock(b);
           if (building != null && building.getTeam() != TeamUtils.getMonsterTeam()) {
               data.setTarget(new BuildingTarget(building, building.getNearestBlock(monster.getLocation()).getLocation(), 6));
               LoggerUtils.debug("found");
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
