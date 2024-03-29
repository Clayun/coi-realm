package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.runnable.AttackGoalTask;
import com.mcylm.coi.realm.tools.attack.AttackGoal;
import com.mcylm.coi.realm.tools.attack.Commandable;
import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.target.TargetType;
import com.mcylm.coi.realm.tools.attack.target.impl.BuildingTarget;
import com.mcylm.coi.realm.tools.attack.target.impl.EntityTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.tools.data.metadata.EntityData;
import com.mcylm.coi.realm.tools.goals.citizens.NPCFeedFoodBehavior;
import com.mcylm.coi.realm.tools.goals.citizens.NPCLookForTargetGoal;
import com.mcylm.coi.realm.tools.goals.citizens.NPCMoveToTargetPointGoal;
import com.mcylm.coi.realm.tools.goals.citizens.NPCSimpleMeleeAttackGoal;
import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.utils.DamageUtils;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import lombok.Setter;
import me.lucko.helper.Events;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
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

    private AttackGoal goal = null;


    public static void registerListener() {

        // 监听被战士攻击之后的伤害处理（适用于原版AI的攻击）
        Events.subscribe(EntityDamageByEntityEvent.class).handler(e -> {


            // 被攻击者
            Entity entity = e.getEntity();
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
                if (projectile.getShooter() instanceof Player player) {
                    if (TeamUtils.getNPCTeam(entity) != null && TeamUtils.getNPCTeam(entity) == TeamUtils.getTeamByPlayer(player)) {
                        e.setCancelled(true);
                        return;
                    }

                }
            }


            // 相同队伍的，不攻击
            if (entity instanceof Player player) {
                if (npc != null && npc.getTeam() == TeamUtils.getTeamByPlayer(player)) {
                    e.setCancelled(true);
                    return;
                }
            }

            // 玩家打的也不设为目标
            if (e.getDamager() instanceof Player player) {
                if (TeamUtils.getNPCTeam(entity) != null && TeamUtils.getNPCTeam(entity) == TeamUtils.getTeamByPlayer(player)) {
                    e.setCancelled(true);
                    return;
                }
            }

            // 两个NPC是相同队伍的，不攻击
            if (TeamUtils.getNPCTeam(entity) != null) {
                if (npc != null && TeamUtils.getNPCTeam(entity) == npc.getTeam()) {
                    e.setCancelled(true);
                    return;
                }
            }

            if (npc != null) {
                // 在攻击伤害范围内，随机产生伤害
                double damage = DamageUtils.getRandomDamage(npc);
                damage = damage * 2;
                // 直接赋值伤害
                e.setDamage(damage);
                ((LivingEntity) entity).damage(damage);
//                LoggerUtils.debug("远程暴击伤害：" + damage);
            }

            // 将攻击者设置为目标
            @Nullable COINpc victim = EntityData.getNpcByEntity(e.getEntity());
            if (victim != null) {
                if (victim instanceof COISoldierCreator soldier
                        && target instanceof LivingEntity perpetrator) {
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


        NPCMoveToTargetPointGoal moveGoal = new NPCMoveToTargetPointGoal(this);
        NPCSimpleMeleeAttackGoal attackGoal = new NPCSimpleMeleeAttackGoal(this);
        NPCLookForTargetGoal targetGoal = new NPCLookForTargetGoal(this);
        NPCFeedFoodBehavior teamBehavior = new NPCFeedFoodBehavior(this);

        // TODO 还原一个与Paper Goal API一样的Goal
        getNpc().getDefaultGoalController().addGoal(new Goal() {
            int tick = 0;

            @Override
            public void reset() {
                tick = 0;
            }

            @Override
            public void run(GoalSelector selector) {

                if (target != null && target.isDead()) {
                    setTarget(null);
                    // entity.setTarget(null);
                }
                if (target != null && isAlive() && target.getTargetLocation().distance(getLocation()) > getCoiNpc().getAlertRadius() * 1.5) {
                    setTarget(null);
                }
                //if (tick++ > 20) reset();
                //if (isAlive() && tick % 1 == 0) {
                // selector.select(teamBehavior);
                if (!executeBehavior(moveGoal)) {
                    executeBehavior(targetGoal);
                    executeBehavior(attackGoal);
                }

                executeBehavior(teamBehavior);
                //}
            }

            private boolean executeBehavior(BehaviorGoalAdapter behavior) {
                if (behavior.shouldExecute()) {
                    behavior.run();
                    return true;
                }
                return false;
            }


            @Override
            public boolean shouldExecute(GoalSelector goalSelector) {
                return true;
            }
        }, 100);
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
                b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 1);
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

//                            LoggerUtils.debug("found");
                            // attack(entity);
                            break;
                        }
                    }

                }
            }
            if (target == null) {
                for (COIBuilding building : LocationUtils.selectionBuildingsByDistance(getLocation(), radius, EnumSet.of(getCoiNpc().getTeam().getType()), true)) {
                    if (building.getType() != COIBuildingType.WALL_NORMAL) {
                        setTarget(new BuildingTarget(building, building.getNearestBlock(getLocation()).getLocation(), 6));
                    }
                }
            }


        }

        fighting = needFight;
    }

    @Override
    public void setTargetDirectly(Target target) {

        if (target != null && target.getType() == TargetType.ENTITY) {
            Mob mob = (Mob) getNpc().getEntity();
            EntityTarget entityTarget = (EntityTarget) target;
            if (isAlive() && mob.getTarget() != entityTarget.getEntity()) {
                mob.setTarget(entityTarget.getEntity());
            }
        }
        this.target = target;
    }

    @Override
    public void damage(Target target, double damage, Location attackLocation) {

        // 先判断是否是生物
        if (target.getType() == TargetType.ENTITY) {
            EntityTarget entityTarget = (EntityTarget) target;
            entityTarget.getEntity().damage(damage,this.npc.getEntity());
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

        // 攻速3的弩
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        // 获取弩的物品元数据
        ItemMeta meta = crossbow.getItemMeta();
        // 获取装填速度附魔
        Enchantment quickChargeEnchantment = Enchantment.QUICK_CHARGE;
        // 设置装填速度附魔的等级
        int enchantmentLevel = 3; // 设置附魔的等级，这里的值可以根据需要进行调整
        meta.addEnchant(quickChargeEnchantment, enchantmentLevel, true);
        // 将修改后的元数据应用到弩的物品栈
        crossbow.setItemMeta(meta);

        npcEntity.getEquipment().setItemInMainHand(
                new Random().nextBoolean() ? crossbow : new ItemStack(Material.IRON_SWORD));


                        // Bukkit.getMobGoals().addGoal(npcEntity, 1, new NPCFollowTeamGoal(this));
        // Bukkit.getMobGoals().addGoal(npcEntity, 0, new NPCSimpleMeleeAttackGoal(this));
        // Bukkit.getMobGoals().addGoal(npcEntity, 0, new NPCLookForTargetGoal(this));


        // 追击/跟随时，移动速度加快
        LivingEntity entity = (LivingEntity) npc.getEntity();
        // 获取当前移动速度
        double currentSpeed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
        // 设置移动速度为 1.3倍速度
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(currentSpeed * 1.3);

    }

    @Override
    public void findPath(Location location) {
        if (!isAlive()) {
            return;
        }

        if (!npc.isSpawned()) {
            return;
        }

        if (currentPath != null && currentPath.getCurrentDestination() != null) {
            if (currentPath.getCurrentDestination().distance(location) < 0.1) {
                return;
            }
        }

        if (findPathCooldown++ > 2) {
            findPathCooldown = 0;
        } else {
            return;
        }

        npc.faceLocation(location);
        Navigator navigator = npc.getNavigator();
        navigator.getLocalParameters()
                .stuckAction(null)
                .useNewPathfinder(useNewPathfinder());

        navigator.setTarget(location);
    }
}
