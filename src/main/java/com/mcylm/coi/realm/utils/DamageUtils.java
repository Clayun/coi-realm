package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.model.COINpc;

import java.util.Random;

/**
 * 伤害计算工具类
 */
public class DamageUtils {

    public static double getRandomDamage(COINpc npc){
        Random rand = new Random();

        double minDamage = npc.getMinDamage() + npc.getLevel();
        double maxDamage = npc.getMaxDamage() + npc.getLevel();



        // 在攻击伤害范围内，随机产生伤害
        double damage = rand.nextInt((int) ((maxDamage + 1) - minDamage)) + minDamage;

        return damage;
    }

}
