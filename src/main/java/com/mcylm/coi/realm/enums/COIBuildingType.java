package com.mcylm.coi.realm.enums;

import lombok.Getter;

/**
 * 建筑类型
 */
@Getter
public enum  COIBuildingType {

    MILL("MILL","磨坊"),
    STOPE("STOPE","矿场"),
    ;

    private String code;
    private String name;

    COIBuildingType(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
