package com.mcylm.coi.realm.enums;

/**
 * 服务器状态枚举
 */
public enum COIServerMode {

    DEVELOP("develop","开发维护"),
    RELEASE("release","正常运营"),
    ;

    private String code;
    private String name;

    COIServerMode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static COIServerMode parseCode(String code) {
        for (COIServerMode value : COIServerMode.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
