package com.mcylm.coi.realm.tools.building;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;


@Data
@AllArgsConstructor
public class COIPaster {

    //每次建筑几个方块
    private int unit;

    //几秒建筑一次
    private long interval;

    //世界名称
    private String worldName;

    //建筑基点位置
    private Location location;

    //建筑结构体
    private COIStructure structure;

    //是否建造空气方块
    private boolean withAir;
}
