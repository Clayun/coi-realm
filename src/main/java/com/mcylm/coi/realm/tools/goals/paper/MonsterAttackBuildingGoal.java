package com.mcylm.coi.realm.tools.goals.paper;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.tools.data.metadata.MonsterData;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.entity.Monster;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class MonsterAttackBuildingGoal implements Goal<Monster> {

    private Monster entity;

    int damage;

    int attackCooldown = 0;

    int tick = 0;
    public MonsterAttackBuildingGoal(Monster entity, int damage) {
        this.entity = entity;
        this.damage = damage;
    }

    @Override
    public boolean shouldActivate() {
        return !entity.isDead();
    }

    @Override
    public void tick() {

        if (tick++ > 15) {
            tick = 0;
            MonsterData data = MonsterData.getDataByEntity(entity);

            if (entity.getTarget() == null || entity.getTarget().isDead()) {
                if (data.getTarget() != null && !data.getTarget().isDead()) {
                    entity.getPathfinder().findPath(data.getTarget().getTargetLocation());
                }
            }
        }
        if (attackCooldown-- < 0) {
            for (Block b : LocationUtils.selectionRadiusByDistance(entity.getLocation().getBlock(), 3, 3)) {
                COIBuilding building = BuildData.getBuildingByBlock(b);
                if (building != null && building.getTeam() != TeamUtils.getMonsterTeam()) {
                    entity.swingMainHand();
                    building.damage(entity, damage, b);
                    attackCooldown = 25;
                    b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 1);
                    break;
                }
            }
        }
    }

    @Override
    public @NotNull GoalKey<Monster> getKey() {
        return GoalKey.of(Monster.class, Entry.getNamespacedKey("attack_building"));
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.UNKNOWN_BEHAVIOR);
    }
}
