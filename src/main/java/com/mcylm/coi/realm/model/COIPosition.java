package com.mcylm.coi.realm.model;

import lombok.Data;

/**
 * 二维位置相对坐标系
 *
 */
@Data
public class COIPosition {

    // 第几行
    private Integer row;

    // 第几列
    private Integer column;
}
