package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Material;

/**
 * 队伍类型
 */
@Getter
@AllArgsConstructor
public enum COIScoreType {

    // 奖励
    KILL_ENTITY("击杀奖励",
            "小队玩家击杀敌方的人员产生的奖励",
            "击杀敌方单位获得奖励积分",
            10),

    DESTROY_BUILDING("拆除奖励",
            "小队玩家拆除敌方建筑产生的奖励",
            "拆除敌方建筑物获得奖励积分",
            50), // 已完成埋点

    BUILD("建造奖励",
            "玩家建造任意一个建筑产生的奖励",
            "建造了一座建筑获得奖励积分",
            50), // 已完成埋点

    UPGRADE_BUILDING("升级奖励",
            "玩家升级一座建筑产生的奖励",
            "升级了一座建筑获得奖励积分",
            50), // 已完成埋点

    VICTORY("胜利奖",
            "小队获得最后的胜利",
            "的小队获得最后的胜利！获得奖励积分",
            200), // 已完成埋点

    // 惩罚
    DEATH_PUNISH("死亡惩罚",
            "玩家死亡每次死亡产生的惩罚",
            "死亡了，惩罚扣除积分",
            5),
    ;

    // 积分变动类型名称
    private String name;
    // 备注
    private String remark;
    // 文本
    private String description;
    // 积分（可增可减）
    private double score;


}
