package com.mcylm.coi.realm.enums;

import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

/**
 * 解锁类型
 */
@Getter
@AllArgsConstructor
public enum COIHeadType {

    // 上锁的箱子
    LOCK_CHEST(
        "LOCK_CHEST",
        "上锁的箱子",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFkOTQzZDA2MzM0N2Y5NWFiOWU5ZmE3NTc5MmRhODRlYzY2NWViZDIyYjA1MGJkYmE1MTlmZjdkYTYxZGIifX19"
        ),

    ;


    // CODE
    private String code;
    // 未解锁时显示的名称 name
    private String name;
    // GUI显示的材质
    private String textures;


}
