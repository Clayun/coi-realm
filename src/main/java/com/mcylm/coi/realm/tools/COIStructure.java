package com.mcylm.coi.realm.tools;

import lombok.Data;

import java.util.List;

/**
 * 建筑结构
 */
@Data
public class COIStructure {

    //建筑名称
    private String name;

    //建筑文件名称
    private String fileName;

    //建筑长
    private Integer length;

    //建筑宽
    private Integer width;

    //建筑高
    private Integer height;

    //建筑方块集合
    private List<COIBlock> blocks;

}
