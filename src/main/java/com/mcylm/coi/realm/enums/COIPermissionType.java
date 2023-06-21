package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum COIPermissionType {

    SKIN_PERMISSION("coi.skin.","皮肤系列权限"),

    ;


    // CODE
    private String code;
    // 名称 name
    private String name;

}
