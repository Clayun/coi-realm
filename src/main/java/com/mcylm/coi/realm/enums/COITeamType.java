package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;

/**
 * 队伍类型
 */
@Getter
@AllArgsConstructor
public enum  COITeamType {

    RED("RED","红队","&c", NamedTextColor.RED,ChatColor.RED,Material.RED_TERRACOTTA,Color.RED,12),
    YELLOW("YELLOW","黄队","&6",NamedTextColor.YELLOW,ChatColor.YELLOW,Material.YELLOW_TERRACOTTA,Color.YELLOW,14),
    GREEN("GREEN","绿队","&a",NamedTextColor.GREEN,ChatColor.GREEN,Material.GREEN_TERRACOTTA,Color.GREEN,16),
    BLUE("BLUE","蓝队","&b",NamedTextColor.BLUE,ChatColor.BLUE,Material.BLUE_TERRACOTTA,Color.BLUE,30),
    BLACK("BLACK","黑队","&8",NamedTextColor.DARK_GRAY,ChatColor.DARK_GRAY,Material.BLACK_TERRACOTTA,Color.BLACK,32),
    PURPLE("PURPLE","紫队","&5",NamedTextColor.LIGHT_PURPLE,ChatColor.LIGHT_PURPLE,Material.PURPLE_TERRACOTTA,Color.PURPLE,34),

    MONSTER("WHITE", "野怪", "&r", NamedTextColor.WHITE,ChatColor.WHITE, Material.WHITE_TERRACOTTA, Color.WHITE, -1);
    ;

    // CODE
    private String code;
    // 小队名称
    private String name;
    // 颜色代码
    private String color;
    // NBT颜色
    private TextColor textColor;
    // TEAM的颜色
    private ChatColor chatColor;
    // 特殊颜色方块，
    private Material blockColor;
    // 队伍皮革装备颜色
    private Color leatherColor;
    // 在选择队伍界面的位置
    private Integer slot;


}
