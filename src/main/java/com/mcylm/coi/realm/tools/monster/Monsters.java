package com.mcylm.coi.realm.tools.monster;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.data.metadata.MonsterData;
import com.mcylm.coi.realm.tools.goals.paper.MonsterAttackBuildingGoal;
import com.mcylm.coi.realm.tools.goals.paper.MonsterLookForBuildingTargetGoal;
import com.mcylm.coi.realm.tools.map.COIMobSpawnPoint;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

public class Monsters {

    // 每个点每次最多生成的怪物数量
    private static int maxMonsterPerLocation = 8;

    public static void spawnAll(int round) {
        for (COIMobSpawnPoint point : Entry.getMapData().getMobSpawnPoints()) {
            spawnZombie(point.getLocation(), round);
        }
    }

    public static void spawnZombie(Location location, int round) {

        // 怪物数量
        int monsterNum = round;
        if(monsterNum >= maxMonsterPerLocation){
            monsterNum = maxMonsterPerLocation;
        }

        for(int i = 0; i < monsterNum;i++){
            Zombie zombie = location.getWorld().spawn(location, Zombie.class);
            configureMonsterGoalsAndBehaviors(zombie,round);
        }
    }

    public static void configureMonsterGoalsAndBehaviors(Monster monster,int round) {

        // 增加的倍率,每次在上一次的基础上增加 2%
        Double percent = 1 + (round * 0.02);
        // 根据回合数自动升级血量，伤害，移动速度
        // 移动速度
        double speed = monster.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * percent;
        monster.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);

        // 攻击伤害
        double damage = monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue() * percent;
        monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);

        // 血量
        double health = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * percent;
        monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        monster.setHealth(health);

        for(Player player : Bukkit.getOnlinePlayers()){
            LoggerUtils.sendMessage("-------- 第 "+round+" 回合预警 --------",player);
            LoggerUtils.sendMessage("怪物血量："+health,player);
            LoggerUtils.sendMessage("怪物攻击："+damage,player);
            LoggerUtils.sendMessage("怪物速度："+speed,player);
            LoggerUtils.sendMessage("-------- 第 "+round+" 回合预警 --------",player);
        }


        if (monster instanceof Zombie zombie) {
            zombie.setShouldBurnInDay(false);
        }

        if (monster instanceof Skeleton skeleton) {
            skeleton.setShouldBurnInDay(false);
        }

        monster.setRemoveWhenFarAway(false);

        TeamUtils.getMonsterTeam().addEntityToScoreboard(monster);
        MobGoals goals = Bukkit.getMobGoals();
        monster.setMetadata("monsterData", new MonsterData());
        goals.addGoal(monster,0, new MonsterAttackBuildingGoal(monster, 8));

        new MonsterLookForBuildingTargetGoal(monster);

    }

}
