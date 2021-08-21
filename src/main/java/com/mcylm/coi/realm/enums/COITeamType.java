package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;

/**
 * 队伍类型
 */
@Getter
@AllArgsConstructor
public enum  COITeamType {

    RED("RED","红队","&c",Material.RED_TERRACOTTA,Color.RED),
    YELLOW("YELLOW","黄队","&6",Material.YELLOW_TERRACOTTA,Color.YELLOW),
    GREEN("GREEN","绿队","&a",Material.GREEN_TERRACOTTA,Color.GREEN),
    BLUE("BLUE","蓝队","&b",Material.BLUE_TERRACOTTA,Color.BLUE),
    BLACK("BLACK","黑队","&0",Material.BLACK_TERRACOTTA,Color.BLACK),
    PURPLE("PURPLE","紫队","&5",Material.PURPLE_TERRACOTTA,Color.PURPLE),

    ;

    // CODE
    private String code;
    // 小队名称
    private String name;
    // 颜色代码
    private String color;
    // 特殊颜色方块，
    private Material blockColor;
    // 队伍皮革装备颜色
    private Color leatherColor;


}
