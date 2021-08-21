package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * 游戏状态
 */
@Getter
@AllArgsConstructor
public enum COIGameStatus {

    WAITING("WAITING","等待中","等待中可以选择或更换队伍"),
    GAMING("GAMING","游戏中","游戏进行中的状态"),
    STOPPING("STOPPING","结算中","游戏已结束，正在给玩家结算奖励"),
    ;

    // 状态CODE
    private String code;
    // 状态名称
    private String name;
    // 状态备注
    private String remark;
}
