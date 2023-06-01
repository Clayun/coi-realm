package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

/**
 * 建筑类型
 */
@Getter
@AllArgsConstructor
public class COIBuildingType {

    public static final COIBuildingType MILL = new COIBuildingType(
            "MILL",
            "磨坊",
            Material.GOLDEN_HOE,
            """
            磨坊是食物收集类建筑.建造完成后会生成一个农民,
            农民会全自动种植小麦并使用骨粉催熟,当小麦成熟后
            农民会将其采集并制作成面包并放入磨坊的箱子中.
            请注意:每个NPC都需要食物补充能量,磨坊的是非常重要的建筑""",
            5,
            5L);

    public static final COIBuildingType STOPE = new COIBuildingType(
            "STOPE",
            "矿场",
            Material.DIAMOND_PICKAXE,
            """
            矿场是资源收集类建筑.建造完成后会生成一个矿工,
            矿工会全自动收集矿物,并存入矿场的箱子中,
            收集的资源可用于建造新的建筑,或者给战士制作装备.""",
            5,
            5L);

    public static final COIBuildingType MILITARY_CAMP = new COIBuildingType(
            "MILITARY_CAMP",
            "军营",
            Material.IRON_SWORD,
            """
            兵营是战斗类建筑,建造完成后会生成一个战士,
            战士会默认自动巡逻,当发现敌方战士或者是敌方建筑时,
            会自动攻击敌方单位""",
            5,
            5L);

    public static final COIBuildingType WALL_NORMAL = new COIBuildingType(
            "WALL_NORMAL",
            "普通城墙",
            Material.BRICK_WALL,
            """
            城墙是防卫类建筑,需要建造多个城墙点来保卫建筑""",
            5,
            5L);

    public static final COIBuildingType DOOR_NORMAL = new COIBuildingType(
            "DOOR_NORMAL",
            "城门",
            Material.IRON_DOOR,
            """
            城门是防卫类建筑""",
            5,
            5L);

    public static final COIBuildingType TURRET_NORMAL = new COIBuildingType(
            "TURRET",
            "防御炮塔",
            Material.AMETHYST_BLOCK,
            """
            防御炮塔会自动检测周围的地方单位,并自动攻击.
            小提示：看到敌方防御塔,可以尝试躲找个掩体,
            在掩体后面是不会被防御塔攻击的哦""",
            5,
            5L);

    // CODE
    private String code;
    // 建筑名称 building name
    private String name;
    // GUI显示的材质
    private Material itemType;
    // 建筑介绍
    private String introduce;
    // 每次建筑几个方块
    // blocks number per build
    private int unit;
    // 几tick建造一次
    private long interval;

}
