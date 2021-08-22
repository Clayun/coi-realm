package com.mcylm.coi.realm.model;

import com.mcylm.coi.realm.tools.npc.impl.COIHuman;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;


@Data
@AllArgsConstructor
public class COIPaster {

    // 是否已完成
    private boolean complete = false;

    // 每次建筑几个方块
    private int unit;

    // 几秒建筑一次
    private long interval;

    // 世界名称
    private String worldName;

    // 建筑基点位置
    private Location location;

    // 建筑结构体
    private COIStructure structure;

    // 是否建造空气方块
    private boolean withAir;

    // 小队的特殊颜色方块替换
    private Material blockColor;

    // 建筑物生成的NPC创建工具
    private COINpc npcCreator;
}
