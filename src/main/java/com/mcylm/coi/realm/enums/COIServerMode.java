package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务器状态枚举
 */
@Getter
@AllArgsConstructor
public enum COIServerMode {

    DEVELOP("develop","开发维护"),
    RELEASE("release","正常运营"),
    ;

    private String code;
    private String name;

    /**
     * 通过 code 获取枚举
     * @param code
     * @return
     */
    public static COIServerMode parseCode(String code) {
        for (COIServerMode value : COIServerMode.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
