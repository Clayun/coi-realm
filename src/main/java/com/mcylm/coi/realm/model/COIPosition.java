package com.mcylm.coi.realm.model;

import lombok.*;

/**
 * 二维位置相对坐标系
 *
 */
@Getter @Setter @RequiredArgsConstructor @ToString
public class COIPosition {

    // 第几行
    private Integer row;

    // 第几列
    private Integer column;
}
