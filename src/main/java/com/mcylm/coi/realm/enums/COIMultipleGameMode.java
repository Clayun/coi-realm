package com.mcylm.coi.realm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务器状态枚举
 */
@Getter
@AllArgsConstructor
public enum COIMultipleGameMode {

    PVP("PVP","多人对抗"),
    PVPVE("PVPVE","对人对抗同时对抗怪物"),
    ;

    private String code;
    private String name;

    /**
     * 通过 code 获取枚举
     * @param code
     * @return
     */
    public static COIMultipleGameMode parseCode(String code) {
        for (COIMultipleGameMode value : COIMultipleGameMode.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
