package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
@AllArgsConstructor
@Getter
public enum AttackGoalType {
    GUARD("镇守", Material.SHIELD, "原地镇守"),
    PATROL("巡逻", Material.BOW, "四处转转"),
    ATTACK("主动进攻", Material.IRON_SWORD, "主动进攻"),
    FOLLOW("跟随", Material.NAME_TAG, "跟随命令者"),
    LOCK("锁定", Material.DIAMOND_SWORD, "锁定几个目标进行攻击"),
    GATHER("集合", Material.FEATHER,"将NPC呼唤至命令者"),
    TEAM_FOLLOW("列队跟随", Material.NAME_TAG, "跟随队伍")
    ;
    private String name;
    // GUI显示的材质
    private Material itemType;
    // 介绍
    private String introduce;
}
