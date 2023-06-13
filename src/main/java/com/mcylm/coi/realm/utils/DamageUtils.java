package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.model.COINpc;

import java.util.Random;

/**
 * 伤害计算工具类
 */
public class DamageUtils {

    public static double getRandomDamage(COINpc npc){
        Random rand = new Random();

        // 在攻击伤害范围内，随机产生伤害
        double damage = rand.nextInt((int) ((npc.getMaxDamage() + 1) - npc.getMinDamage())) + npc.getMinDamage();


        return damage;
    }

}
