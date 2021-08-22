package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;

/**
 * GUI的类型
 */
@Getter
@AllArgsConstructor
public enum  COIGUIType {

    CHOOSE_TEAM_GUI("选择队伍界面",Component.text("选择要加入的小队")),
    ;

    // GUI名称
    private String name;
    // GUI Component
    private Component component;

}
