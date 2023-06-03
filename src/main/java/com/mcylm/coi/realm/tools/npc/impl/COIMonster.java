package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.player.COIPlayer;
import com.mcylm.coi.realm.runnable.AttackGoalTask;
import com.mcylm.coi.realm.tools.attack.AttackGoal;
import com.mcylm.coi.realm.tools.attack.Commandable;
import com.mcylm.coi.realm.tools.attack.impl.PatrolGoal;
import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.target.TargetType;
import com.mcylm.coi.realm.tools.attack.target.impl.BuildingTarget;
import com.mcylm.coi.realm.tools.attack.target.impl.EntityTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.BuildData;
import com.mcylm.coi.realm.tools.data.EntityData;
import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LocationUtils;
import lombok.Getter;
import lombok.Setter;
import me.lucko.helper.Events;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * PVE怪物（正常会刷在野外的几个随机的地方）
 * 会攻击附近的目标小队
 * 优先攻击目标小队的NPC-》玩家-》建筑
 * 死亡掉落一定的物资
 */
@Getter
public class COIMonster extends COIHuman implements Commandable {

    private CompletableFuture<BuildingTarget> targetFuture = null;

    @Setter
    private COIPlayer commander;

    @Setter
    private COITeam targetTeam;

    private AttackGoal goal = new PatrolGoal(this);

    public static void registerListener() {
        Events.subscribe(EntityDamageByEntityEvent.class).handler(e -> {
            @Nullable COINpc npc = EntityData.getNpcByEntity(e.getEntity());
            Entity target = e.getDamager();
            if (npc instanceof COISoldierCreator creator) {

                if (e.getDamager() instanceof Projectile projectile) {
                    if (projectile.getShooter() instanceof LivingEntity s) {
                        target = s;
                    }
                }

                if (target instanceof LivingEntity livingEntity) {
                    if (livingEntity instanceof Player player && player.getGameMode() == GameMode.CREATIVE) {
                        return;
                    }
                    ((COIMonster) creator.getNpc()).setTarget(new EntityTarget(livingEntity, 8));

                }
            }

        });
    }
    // 周围发现敌人，进入战斗模式
    private boolean fighting = false;

    private Target target;

    public COIMonster(COISoldierCreator npcCreator) {
        super(npcCreator);

    }

    /**
     * 警戒
     */
    private void alert() {



    }
    /**
     * 攻击实体
     */
    private void meleeAttackTarget() {
        if (target == null) return;

        // 对生物体直接产生伤害
        Random rand = new Random();

        // 在攻击伤害范围内，随机产生伤害
        double damage = rand.nextInt((int) ((getCoiNpc().getMaxDamage() + 1) - getCoiNpc().getMinDamage())) + getCoiNpc().getMinDamage();


        if (getNpc().getEntity().getLocation().distance(target.getTargetLocation()) <= 3 && target.getType() == TargetType.ENTITY) {

            // 挥动手
            ((LivingEntity) getNpc().getEntity()).swingMainHand();


            damage(target, damage, target.getTargetLocation());

        }


        // 攻击建筑物
        for (Block b : LocationUtils.selectionRadiusByDistance(getLocation().getBlock(), 3, 3)) {
            COIBuilding building = BuildData.getBuildingByBlock(b);

            // 无差别拆家
            if (building != null) {
                ((LivingEntity) getNpc().getEntity()).swingMainHand();
                building.damage(getNpc().getEntity(), (int) damage, b);
                b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND,1);

                break;
            }
        }




        findPath(target.getTargetLocation());

    }


    @Override
    public void move() {
        super.move();

        //警戒周围
        meleeAttackTarget();

        if (target != null && target.isDead()) target = null;
    }

    @Override
    public void dead() {
        super.dead();
        if (isAlive()) {
            return;
        }
        target = null;
    }

    @Override
    public void lookForEnemy(int radius) {

        if (radius == -1) {
            radius = (int) getCoiNpc().getAlertRadius();
        }

        List<Entity> nearByEntities = getNearByEntities(radius);

        if (nearByEntities.isEmpty()) {
            return;
        }

        // 是否需要开启战斗模式
        boolean needFight = false;

        for (Entity entity : nearByEntities) {

            if (getCoiNpc().getEnemyPlayers() != null
                    && !getCoiNpc().getEnemyPlayers().isEmpty()) {
                if (entity.getType().equals(EntityType.PLAYER)) {
                    Player player = (Player) entity;

                    if (getCoiNpc().getEnemyPlayers().contains(player.getName()) && player.getGameMode() != GameMode.CREATIVE) {
                        // 找到敌对玩家，进入战斗状态
                        needFight = true;
                        // 发动攻击
                        if (target == null) {
                            setTarget(new EntityTarget(player, 6));
                            // attack(player);
                        }
                        break;
                    }

                }
            }

            @Nullable COINpc data = EntityData.getNpcByEntity(entity);
            if (data != null && data.getTeam() != getCoiNpc().getTeam()) {

                needFight = true;
                // 发动攻击
                if (target == null) {
                    setTarget(new EntityTarget((LivingEntity) entity, 6));
                    // attack(player);
                    break;
                }

            }

            if (getCoiNpc().getEnemyEntities() != null
                    && !getCoiNpc().getEnemyEntities().isEmpty()) {

                if (getCoiNpc().getEnemyEntities().contains(entity.getType())) {
                    // 找到敌对生物，进入战斗状态
                    needFight = true;
                    // 发动攻击
                    // 如果NPC设置了主动攻击，就开始战斗
                    if (getCoiNpc().isAggressive()) {
                        if (target == null) {
                            setTarget(new EntityTarget((LivingEntity) entity));
                            // attack(entity);
                            break;
                        }
                    }

                }
            }
            if (target == null && (targetFuture == null || targetFuture.isDone())) {
                int finalRadius = radius;
                targetFuture = CompletableFuture.supplyAsync(() -> {
                    for (Block b : LocationUtils.selectionRadiusByDistance(getLocation().getBlock(), finalRadius, finalRadius)) {
                        COIBuilding building = BuildData.getBuildingByBlock(b);
                        if (building != null && building.getTeam() != getCoiNpc().getTeam() && building.getType() != COIBuildingType.WALL_NORMAL) {
                            return new BuildingTarget(building, building.getNearestBlock(getLocation()).getLocation());
                        }
                    }
                    return null;
                });
                targetFuture.thenAccept(result -> {
                    target = result;
                });
            }


        }

        fighting = needFight;
    }

    @Override
    public void setTargetDirectly(Target target) {

        this.target = target;
    }

    @Override
    public void damage(Target target, double damage, Location attackLocation) {
        if (target.getType() == TargetType.ENTITY) {
            EntityTarget entityTarget = (EntityTarget) target;
            entityTarget.getEntity().damage(damage, getNpc().getEntity());
        } else if (target.getType() == TargetType.BUILDING) {
            BuildingTarget buildingTarget = (BuildingTarget) target;
            buildingTarget.getBuilding().damage(getNpc().getEntity(), (int) damage, attackLocation.getBlock());
        }
    }


    @Override
    public void setGoal(AttackGoal goal) {
        if (this.goal != null) {
            AttackGoalTask.getGoalSet().remove(this.goal);
            this.goal.stop();
        }
        this.goal = goal;
        AttackGoalTask.getGoalSet().add(goal);
    }

    @Override
    public AttackGoal getGoal() {
        return goal;
    }
}
