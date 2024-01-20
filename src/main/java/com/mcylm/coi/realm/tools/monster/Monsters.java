package com.mcylm.coi.realm.tools.monster;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.google.common.collect.Maps;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.MonsterData;
import com.mcylm.coi.realm.tools.goals.paper.MonsterAttackBuildingGoal;
import com.mcylm.coi.realm.tools.goals.paper.MonsterLookForBuildingTargetGoal;
import com.mcylm.coi.realm.tools.map.COIMobSpawnPoint;
import com.mcylm.coi.realm.tools.monster.custom.CustomMonster;
import com.mcylm.coi.realm.tools.monster.custom.impl.GiantMonster;
import com.mcylm.coi.realm.tools.monster.custom.impl.VanillaMonster;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Monsters {

    // 每个点每次最多生成的怪物数量
    private static int maxMonsterPerLocation = 16;

    private static HashMap<Player,Integer> roundNotice = new HashMap<>();
    private static Map<CustomMonster, Integer> entityTypes = new HashMap<>();

    public static final VanillaMonster ZOMBIE = new VanillaMonster(EntityType.ZOMBIE);
    public static final VanillaMonster SKELETON = new VanillaMonster(EntityType.SKELETON);
    public static final VanillaMonster CREEPER = new VanillaMonster(EntityType.CREEPER);
    public static final VanillaMonster SPIDER = new VanillaMonster(EntityType.SPIDER);
    public static final VanillaMonster WITHER_SKELETON = new VanillaMonster(EntityType.WITHER_SKELETON);
    public static final VanillaMonster PILLAGER = new VanillaMonster(EntityType.RAVAGER);
    public static final VanillaMonster VINDICATOR = new VanillaMonster(EntityType.VINDICATOR);
    public static final VanillaMonster RAVAGER = new VanillaMonster(EntityType.RAVAGER);
    public static final GiantMonster GIANT = new GiantMonster();


    public static void spawnAll(int round) {
        entityTypes.put(ZOMBIE, 50);
        for (COIMobSpawnPoint point : Entry.getMapData().getMobSpawnPoints()) {
            spawnEntity(point.getLocation(), round);
            if (round > 6) {
                entityTypes.put(SKELETON, 20);
            }
            if (round > 10) {
                entityTypes.put(CREEPER, 30);
            }
            if (round > 20) {
                entityTypes.put(SPIDER, 40);
            }
            if (round > 30) {
                entityTypes.put(WITHER_SKELETON, 40);
            }
            if (round > 40) {
                entityTypes.put(PILLAGER, 20);
            }
            if (round > 50) {
                entityTypes.put(VINDICATOR, 30);
            }
            if (round > 60) {
                entityTypes.put(RAVAGER, 15);
            }
            if (round > 60) {
                entityTypes.put(GIANT, 10);
            }
        }

    }


    public static void spawnEntity(Location location, int round) {

        // 怪物数量
        int monsterNum = (round / 5) + 1;
        if(monsterNum >= maxMonsterPerLocation){
            monsterNum = maxMonsterPerLocation;
        }

        for(int i = 0; i < monsterNum;i++){
            List<COIBuilding> coiBuildings = LocationUtils.selectionBuildingsByDistance(location, 2, EnumSet.of(COITeamType.MONSTER), true);

            for(COIBuilding coiBuilding : coiBuildings){
                location.createExplosion(3,false,false);
                coiBuilding.destroy(true);
            }
            CustomMonster type = selectCustomMonster(entityTypes);
            if (type == null) {
                continue;
            }
            Monster monster = (Monster) location.getWorld().spawnEntity(location, type.getType());
            configureMonsterGoalsAndBehaviors(monster, type,round);
        }
    }

    public static void configureMonsterGoalsAndBehaviors(Monster entity,CustomMonster customMonster, int round) {

        // 增加的倍率,每次在上一次的基础上增加 2%
        Double percent = 1 + (round * 0.02);
        Double basicHealth = entity.getMaxHealth()/2;
        Double basicDamage = 5d;
        // 根据回合数自动升级血量，伤害，移动速度
        // 移动速度
        double speed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * percent;

        if(speed >= 0.35){
            speed = 0.35d;
        }

        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);

        // 攻击伤害
        Double damage = basicDamage * percent;
        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);

        // 血量
        double health = basicHealth * percent;
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        entity.setHealth(health);

        // 通知玩家怪物强度
        for(Player player : Bukkit.getOnlinePlayers()){

            String healthMessage = "怪物血量：" + new BigDecimal(health).setScale(2, RoundingMode.HALF_UP);
            String damageMessage = "怪物攻击：" + new BigDecimal(damage).setScale(2, RoundingMode.HALF_UP);
            String speedMessage = "怪物速度：" + new BigDecimal(speed).setScale(2, RoundingMode.HALF_UP);

            if(roundNotice.get(player) == null){
                LoggerUtils.sendMessage("-------- 第 "+round+" 回合预警 --------",player);
                LoggerUtils.sendMessage(healthMessage,player);
                LoggerUtils.sendMessage(damageMessage,player);
                LoggerUtils.sendMessage(speedMessage,player);
                LoggerUtils.sendMessage("-------- 第 "+round+" 回合预警 --------",player);
                roundNotice.put(player,round);
            }else{
                Integer currentRound = roundNotice.get(player);
                if(currentRound != round){
                    LoggerUtils.sendMessage("-------- 第 "+round+" 回合预警 --------",player);
                    LoggerUtils.sendMessage(healthMessage,player);
                    LoggerUtils.sendMessage(damageMessage,player);
                    LoggerUtils.sendMessage(speedMessage,player);
                    LoggerUtils.sendMessage("-------- 第 "+round+" 回合预警 --------",player);
                    roundNotice.put(player,round);
                }

            }

        }


        if (entity instanceof Zombie zombie) {
            zombie.setShouldBurnInDay(false);
        }

        if (entity instanceof Skeleton skeleton) {
            skeleton.setShouldBurnInDay(false);
        }

        entity.setRemoveWhenFarAway(false);

        TeamUtils.getMonsterTeam().addEntityToScoreboard(entity);
        MobGoals goals = Bukkit.getMobGoals();
        entity.setMetadata("monsterData", new MonsterData());
        goals.addGoal(entity,0, new MonsterAttackBuildingGoal(entity, damage.intValue()));

        new MonsterLookForBuildingTargetGoal(entity);

        customMonster.spawn(entity);

    }

    public static CustomMonster selectCustomMonster(Map<CustomMonster, Integer> weights) {
        int totalWeight = 0;

        for (int weight : weights.values()) {
            totalWeight += weight;
        }

        Random random = new Random();
        int randomValue = random.nextInt(totalWeight);

        for (Map.Entry<CustomMonster, Integer> entry : weights.entrySet()) {
            randomValue -= entry.getValue();
            if (randomValue <= 0) {
                return entry.getKey();
            }
        }


        return null;
    }


}
