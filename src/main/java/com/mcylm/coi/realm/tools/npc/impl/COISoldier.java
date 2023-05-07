package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.attack.DamageableAI;
import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.target.TargetType;
import com.mcylm.coi.realm.tools.attack.target.impl.BuildingTarget;
import com.mcylm.coi.realm.tools.attack.target.impl.EntityTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.impl.COIBuilder;
import com.mcylm.coi.realm.tools.data.BuildData;
import com.mcylm.coi.realm.tools.data.EntityData;
import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.utils.FormationUtils;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import lombok.Setter;
import me.lucko.helper.Events;
import net.citizensnpcs.api.ai.tree.Behavior;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 战士
 * 拥有多人自动编排能力，自发的组成阵容
 * 会对敌对阵营的建筑进行破坏，并主动攻击敌对阵营玩家
 * 主动跟随阵营内拥有将军令的玩家
 */
@Getter
@Setter
public class COISoldier extends COIHuman implements DamageableAI {

    private CompletableFuture<BuildingTarget> targetFuture = null;


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
                    ((COISoldier) creator.getNpc()).setTarget(new EntityTarget(livingEntity));
                }
            }

        });
    }
    // 周围发现敌人，进入战斗模式
    private boolean fighting = false;

    private Target target;

    public COISoldier(COISoldierCreator npcCreator) {
        super(npcCreator);

    }

    /**
     * 警戒
     */
    private void alert() {


        List<Entity> nearByEntities = getNearByEntities(getCoiNpc().getAlertRadius());

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
                            setTarget(new EntityTarget(player));
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
                    setTarget(new EntityTarget((LivingEntity) entity));
                    // attack(player);
                }
                break;


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
                        }
                    }
                    break;
                }
            }
            if (target == null && (targetFuture == null || targetFuture.isDone())) {
                targetFuture = CompletableFuture.supplyAsync(() -> {
                    for (Block b : LocationUtils.selectionRadiusByDistance(getLocation().getBlock(), (int) getCoiNpc().getAlertRadius(), (int) getCoiNpc().getAlertRadius())) {
                        COIBuilding building = BuildData.getBuildingByBlock(b);
                        if (building != null && building.getTeam() != getCoiNpc().getTeam() && building.getType() != COIBuildingType.WALL_NORMAL) {
                            return new BuildingTarget(building);
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


        for (Block b : LocationUtils.selectionRadiusByDistance(getLocation().getBlock(), 3, 3)) {
            COIBuilding building = BuildData.getBuildingByBlock(b);
            if (building != null && building.getTeam() != getCoiNpc().getTeam()) {
                ((LivingEntity) getNpc().getEntity()).swingMainHand();
                building.damage(getNpc().getEntity(), (int) damage, b);
                b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND,1);

                break;
            }
        }




        findPath(target.getTargetLocation());

    }



    /**
     * 行军寻路
     *
     * @param location
     * @param faceLocation
     */
    public void walk(Location location, Location faceLocation) {
        if (getNpc() == null) {
            return;
        }

        if (!getNpc().isSpawned()) {
            return;
        }

        getNpc().faceLocation(faceLocation);
        getNpc().getNavigator().setTarget(location);
    }

    /**
     * 更换NPC跟随的玩家
     *
     * @param newFollowPlayer
     */
    public void changeFollowPlayer(String newFollowPlayer) {
        getCoiNpc().setFollowPlayerName(newFollowPlayer);
    }



    @Override
    public void move() {
        super.move();


        //警戒周围
        alert();

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
    public void onTarget(Target target) {
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
}
