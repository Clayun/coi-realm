package com.mcylm.coi.realm.tools.npc.impl.monster;

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
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import com.mcylm.coi.realm.tools.npc.monster.COIMonsterCreator;
import com.mcylm.coi.realm.utils.GUIUtils;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import lombok.Setter;
import me.lucko.helper.Events;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public abstract class COIMonster extends COIEntity implements Commandable {

    // 是否需要补充能量
    private boolean needCharging = false;

    public COIMonster(COIMonsterCreator npcCreator) {
        super(npcCreator);
        npcCreator.setTeam(TeamUtils.getMonsterTeam());
    }

    protected void initNpcAttributes(COINpc npcCreator) {

        // 恢复血量和饱食度
        initEntityStatus();
        setHunger(Integer.MAX_VALUE);

        // 如果背包未初始化
        if (npcCreator.getInventory() == null) {
            npcCreator.setInventory(GUIUtils.createNpcInventory(3));
        }


        // 设置NPC的名称使用 Hologram
        this.npc.setAlwaysUseNameHologram(true);
        this.npc.setUseMinecraftAI(true);
        this.npc.data().set(NPC.Metadata.KEEP_CHUNK_LOADED, true);
    }


    private CompletableFuture<BuildingTarget> targetFuture = null;

    @Setter
    private LivingEntity commander;

    private AttackGoal goal = new PatrolGoal(this);

    public static void registerListener() {
        Events.subscribe(EntityDamageByEntityEvent.class).handler(e -> {
            @Nullable COINpc npc = EntityData.getNpcByEntity(e.getEntity());
            Entity target = e.getDamager();
            if (npc instanceof COIMonsterCreator creator) {

                if (e.getDamager() instanceof Projectile projectile) {
                    if (projectile.getShooter() instanceof LivingEntity s) {
                        target = s;
                    }
                }

                if (target instanceof LivingEntity livingEntity) {
                    if (livingEntity instanceof Player player && player.getGameMode() == GameMode.CREATIVE) {
                        return;
                    }

                    // 相同队伍的，不攻击
                    if(livingEntity instanceof Player player){
                        if(npc.getTeam() == TeamUtils.getTeamByPlayer(player)){
                            return;
                        }
                    }

                    ((COIMonster) creator.getNpc()).setTarget(new EntityTarget(livingEntity, 8));

                }
            }

        });
    }

    protected Target target;


    /**
     * 攻击实体
     */
    private void meleeAttackTarget() {

        if(!isAlive()){
            return;
        }

        if(getLocation() != null){
            // 攻击建筑
            for (Block b : LocationUtils.selectionRadiusByDistance(getLocation().getBlock(), 3, 3)) {
                COIBuilding building = BuildData.getBuildingByBlock(b);
                if (building != null && building.getTeam() != getCoiNpc().getTeam()) {
                    ((LivingEntity) getNpc().getEntity()).swingMainHand();
                    building.damage(getNpc().getEntity(), (int) getDamage(), b);
                    b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND,1);

                    break;
                }
            }
        }



        if(target != null){
            if (target.getType() == TargetType.BUILDING) {
                findPath(target.getTargetLocation());
            } else if (target.getType() == TargetType.ENTITY){
                EntityTarget entityTarget = (EntityTarget) target;
                Mob mobNpc = ((Mob) getNpc().getEntity());
                if (!(mobNpc.getTarget() == entityTarget.getEntity())) {
                    mobNpc.setTarget(entityTarget.getEntity());
                }
            }
        }



    }

    public abstract int getDamage();


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


    /**
     * 自动充电
     * @return 是否有足够的电量
     */
    private boolean automaticCharging(){

        if(!isAlive()){
            return true;
        }

        // 需要强制充电
        if(needCharging){

            findPath(getCoiNpc().getSpawnLocation());

            if(getLocation().distance(getCoiNpc().getSpawnLocation()) <= 3){
                // 开始充电
                say("休息中...");

                // 最大电量 40
                if(getHunger() < 40){
                    setHunger(getHunger() + 0.5);

                    // 如果开启强制充电，就充满
                    if(needCharging){
                        if(getHunger() >= 40){
                            needCharging = false;
                            return true;
                        }
                    }
                }

            }

            return false;
        }

        // 普通充电
        if(getLocation().distance(getCoiNpc().getSpawnLocation()) <= 3){
            // 开始充电
            say("休息中...");

            // 最大电量 40
            if(getHunger() < 40){
                setHunger(getHunger() + 0.5);
            }else{
                needCharging = false;
            }
        }


        if(getHunger() <= 7){
            say("好累啊，回去休息了");
            needCharging = true;

            return false;
        }

        return true;
    }

    @Override
    public void move() {
        super.move();

        boolean b = automaticCharging();

        if(b){
            //警戒周围
            meleeAttackTarget();
        }



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
