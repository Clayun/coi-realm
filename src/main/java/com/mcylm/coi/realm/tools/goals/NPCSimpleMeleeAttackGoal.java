package com.mcylm.coi.realm.tools.goals;

import com.mcylm.coi.realm.tools.attack.DamageableAI;
import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.target.TargetType;
import com.mcylm.coi.realm.tools.attack.target.impl.EntityTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.utils.DamageUtils;
import com.mcylm.coi.realm.utils.LocationUtils;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Mob;

public class NPCSimpleMeleeAttackGoal extends BehaviorGoalAdapter {


    private Mob entity;

    private DamageableAI ai;

    private int attackCooldown = 0;


    public NPCSimpleMeleeAttackGoal(DamageableAI ai) {
        this.ai = ai;
        this.entity = (Mob) ai.asEntity().getNpc().getEntity();

    }
    @Override
    public void reset() {

    }

    @Override
    public BehaviorStatus run() {


        if (attackCooldown > 0) {
            attackCooldown--;
            return BehaviorStatus.RUNNING;
        }

        // 在攻击伤害范围内，随机产生伤害
        double damage = DamageUtils.getRandomDamage(ai.asEntity().getCoiNpc());

        // 攻击建筑
        for (Block b : LocationUtils.selectionRadiusByDistance(entity.getLocation().getBlock(), 3, 3)) {
            COIBuilding building = BuildData.getBuildingByBlock(b);
            if (building != null && building.getTeam() != ai.asEntity().getCoiNpc().getTeam()) {
                entity.swingMainHand();
                building.damage(entity, (int) damage, b);
                attackCooldown = 25;
                b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 1);
                break;
            }
        }

        Mob npcEntity = entity;

        Target target = ai.getTarget();

        if (target.getType() == TargetType.BUILDING) {
            // TODO 弓箭射击建筑 (创造个临时实体作为目标)
            ai.findPath(target.getTargetLocation());

        }
        if (target.getType() == TargetType.ENTITY && npcEntity.getEquipment().getItemInMainHand().getType() == Material.CROSSBOW) {
            npcEntity.setTarget(((EntityTarget) target).getEntity());
        } else {
            if (ai.asEntity().getNpc().getEntity().getLocation().distance(target.getTargetLocation()) <= 3 && target.getType() == TargetType.ENTITY) {
                // 挥动手
                entity.swingMainHand();
                attackCooldown = 10;
                ai.damage(target, damage, target.getTargetLocation());

                entity.lookAt(target.getTargetLocation());

            }
            ai.findPath(target.getTargetLocation());

        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {

        if (!ai.asEntity().isAlive()) {
            return false;
        }
        this.entity = (Mob)ai.asEntity().getNpc().getEntity();


        if (ai.asEntity().isTooHungryToWork()) {
            return false;
        }



        if (entity.getEquipment().getItemInMainHand().getType() == Material.CROSSBOW) {
            if (ai.getTarget() != null && ai.getTarget().getType() != TargetType.BUILDING) {
                return false;
            }
        }

        if (ai.getTarget() != null && !ai.getTarget().isDead()) {
            return true;
        }

        return false;
    }
}
