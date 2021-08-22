package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

/**
 * 建筑类型
 */
@Getter
@AllArgsConstructor
public enum  COIBuildingType {

    MILL("MILL","磨坊",Material.GOLDEN_HOE
            ,"磨坊是食物收集类建筑，建造完成后会生成一个农民" +
            "，农民会全自动种植小麦，并使用骨粉催熟，当小麦成熟后，农民会将其采集并制作成面包并放入磨坊的箱子中。" +
            "请注意：每个NPC都需要食物补充能量，磨坊的是非常重要的建筑"
            ,5,5L),

    STOPE("STOPE","矿场",Material.DIAMOND_PICKAXE
            ,"矿场是资源收集类建筑，建造完成后会生成一个矿工" +
            "，矿工会全自动收集矿物，并存入矿场的箱子中，收集的资源可用于建造新的建筑，或者给战士制作装备"
            ,5,5L),
    ;

    private String code;
    // 建筑名称
    private String name;
    // GUI显示的材质
    private Material itemType;
    // 建筑介绍
    private String introduce;
    // 每次建筑几个方块
    private int unit;
    // 几tick建造一次
    private long interval;
}
