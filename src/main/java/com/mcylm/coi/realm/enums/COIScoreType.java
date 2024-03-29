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

    // 挖矿奖励
    GOOD_MINER("挖矿奖励",
            "小队玩家主动挖矿产生的奖励",
            "用镐子挖矿获得奖励积分",
            2),

    DESTROY_BUILDING("拆除奖励",
            "小队玩家拆除敌方建筑产生的奖励",
            "拆除敌方建筑物获得奖励积分",
            50), // 已完成埋点

    BUILD("建造奖励",
            "玩家建造任意一个建筑产生的奖励",
            "建造了一座建筑获得奖励积分",
            50), // 已完成埋点

    DESTROY_SELF_BUILDING("拆除己方建筑惩罚",
            "玩家拆除任意一个己方建筑产生的惩罚",
            "拆除了一座己方建筑获得惩罚",
            -50), // 已完成埋点

    BUILD_INFRASTRUCTURE("建造基建奖励",
            "玩家建造任意基建产生的奖励",
            "建造了基建获得奖励积分",
            2), // 已完成埋点

    UPGRADE_BUILDING("升级奖励",
            "玩家升级一座建筑产生的奖励",
            "升级了一座建筑获得奖励积分",
            50), // 已完成埋点

    BEAT_TEAM("击败奖励",
            "小队玩家拆掉其他小队基地产生的奖励",
            "拆除敌方基地获得奖励积分",
            1000),

    VICTORY("胜利奖",
            "小队获得最后的胜利",
            "小队获得最后的胜利！获得奖励积分",
            2000), // 已完成埋点

    // MVP
    MVP("MVP",
            "全场表现最佳",
            "获得MVP奖励积分",
            200),

    SVP("SVP",
            "败方队伍表现最佳",
            "获得SVP奖励积分",
            200),
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
