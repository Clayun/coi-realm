package com.mcylm.coi.realm.model;

import com.mcylm.coi.realm.enums.COIBuildingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class COISkin {

    // CODE
    private String code;
    // 皮肤名称
    private String name;
    // GUI模型
    private String material;
    // 玩家所需权限
    private String permission;
    // 皮肤所对应的建筑
    private String buildingTypeCode;
    // 建筑等级模板文件
    private Map<Integer, String> buildingLevelStructure;
    // NPC的皮肤
    private String npcSkin;

}
