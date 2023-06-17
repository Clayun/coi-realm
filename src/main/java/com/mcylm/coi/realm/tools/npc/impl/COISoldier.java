package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.runnable.AttackGoalTask;
import com.mcylm.coi.realm.tools.attack.AttackGoal;
import com.mcylm.coi.realm.tools.attack.Commandable;
import com.mcylm.coi.realm.tools.attack.impl.PatrolGoal;
import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.target.TargetType;
import com.mcylm.coi.realm.tools.attack.target.impl.BuildingTarget;
import com.mcylm.coi.realm.tools.attack.target.impl.EntityTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.tools.data.metadata.EntityData;
import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.utils.DamageUtils;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import lombok.Setter;
import me.lucko.helper.Events;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * 战士
 * 会对敌对阵营的建筑进行破坏，并主动攻击敌对阵营玩家
 * 主动跟随阵营内拥有将军令的玩家
 */
@Getter
public class COISoldier extends COIEntity implements Commandable {

    private CompletableFuture<BuildingTarget> targetFuture = null;

    @Setter
    private LivingEntity commander;

    private AttackGoal goal = new PatrolGoal(this);

    public static void registerListener() {

        // 监听被战士攻击之后的伤害处理（适用于原版AI的攻击）
        Events.subscribe(EntityDamageByEntityEvent.class).handler(e -> {

            // 需要反击的目标
            Entity target = e.getDamager();

            @Nullable COINpc npc = EntityData.getNpcByEntity(e.getDamager());

            // 先判断是否是远程攻击

            if (e.getDamager() instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof LivingEntity s) {
                    // 判断是远程攻击
                    npc = EntityData.getNpcByEntity(s);
                    // 先将该NPC设定为反击目标
                    target = s;
                }
            }


            // 被攻击者
            Entity entity = e.getEntity();

            // 相同队伍的，不攻击
            if(entity instanceof Player player){
                if(npc != null && npc.getTeam() == TeamUtils.getTeamByPlayer(player)){
                    e.setCancelled(true);
                    return;
                }
            }

            // 玩家打的也不设为目标
            if(e.getDamager() instanceof Player player){
                if(TeamUtils.getNPCTeam(entity) != null && TeamUtils.getNPCTeam(entity) == TeamUtils.getTeamByPlayer(player)){
                    e.setCancelled(true);
                    return;
                }
            }

            // 两个NPC是相同队伍的，不攻击
            if(TeamUtils.getNPCTeam(entity) != null){
                if(npc != null && TeamUtils.getNPCTeam(entity) == npc.getTeam()){
                    e.setCancelled(true);
                    return;
                }
            }

            if(npc != null){
                // 在攻击伤害范围内，随机产生伤害
                double damage = DamageUtils.getRandomDamage(npc);
                // 直接赋值伤害
                e.setDamage(damage);
                ((LivingEntity) entity).damage(damage);
                LoggerUtils.debug("远程伤害："+damage);
            }

            // 将攻击者设置为目标
            @Nullable COINpc victim = EntityData.getNpcByEntity(e.getEntity());
            if(victim != null){
                if(victim instanceof COISoldierCreator soldier
                        && target instanceof LivingEntity perpetrator){
                    ((COISoldier) soldier.getNpc()).setTarget(new EntityTarget(perpetrator, 8));
                }
            }


        });


    }
    // 周围发现敌人，进入战斗模式
    private boolean fighting = false;

    private Target target;

    public COISoldier(COISoldierCreator npcCreator) {
        super(npcCreator);

        getNpc().setUseMinecraftAI(true);
        getNpc().setAlwaysUseNameHologram(false);
    }

    /**
     * 攻击实体
     */
    private void meleeAttackTarget() {
        if (target == null) return;

        // 在攻击伤害范围内，随机产生伤害
        double damage = DamageUtils.getRandomDamage(getCoiNpc());

        if (getLocation() == null) {
            return;
        }

        // 攻击建筑
        for (Block b : LocationUtils.selectionRadiusByDistance(getLocation().getBlock(), 3, 3)) {
            COIBuilding building = BuildData.getBuildingByBlock(b);
            if (building != null && building.getTeam() != getCoiNpc().getTeam()) {
                ((LivingEntity) getNpc().getEntity()).swingMainHand();
                building.damage(getNpc().getEntity(), (int) damage, b);
                b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND,1);
                break;
            }
        }

        Mob npcEntity = (Mob) getNpc().getEntity();

        if (target.getType() == TargetType.BUILDING) {
            // TODO 弓箭射击建筑 (创造个临时实体作为目标)
            findPath(target.getTargetLocation());

        }
        if (target.getType() == TargetType.ENTITY && npcEntity.getEquipment().getItemInMainHand().getType() == Material.CROSSBOW) {
            npcEntity.setTarget(((EntityTarget) target).getEntity());
        } else {
            if (getNpc().getEntity().getLocation().distance(target.getTargetLocation()) <= 3 && target.getType() == TargetType.ENTITY) {
                // 挥动手
                ((LivingEntity) getNpc().getEntity()).swingMainHand();
                damage(target, damage, target.getTargetLocation());


            }
            findPath(target.getTargetLocation());

        }

    }

    @Override
    public void move() {
        super.move();

        //警戒周围
        meleeAttackTarget();

        if (target != null && target.isDead()) {
            target = null;
            ((Mob) getNpc().getEntity()).setTarget(null);
        }
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
                        if (building != null && building.getTeam() != getCoiNpc().getTeam()) {
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

        // 先判断是否是生物
        if (target.getType() == TargetType.ENTITY) {
            EntityTarget entityTarget = (EntityTarget) target;
            entityTarget.getEntity().damage(damage);
            entityTarget.getEntity().setNoDamageTicks(0);
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

    @Override
    public void spawn(Location location) {
        super.spawn(location);

        getCoiNpc().getInventory().addItem(new ItemStack(Material.LEATHER_HELMET));

        Mob npcEntity = ((Mob) getNpc().getEntity());
        npcEntity.getEquipment().setItemInMainHand(new ItemStack(new Random().nextBoolean() ? Material.CROSSBOW : Material.IRON_SWORD));

//        // 追击/跟随时，移动速度加快
//        LivingEntity entity = (LivingEntity)npc.getEntity();
//        // 获取当前移动速度
//        double currentSpeed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
//        // 设置移动速度为 1.5倍速度
//        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(currentSpeed * 1.5);

    }
}
