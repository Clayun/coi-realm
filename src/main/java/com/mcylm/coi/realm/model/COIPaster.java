package com.mcylm.coi.realm.model;

import com.mcylm.coi.realm.tools.handler.BlockPlaceCondition;
import com.mcylm.coi.realm.tools.handler.BlockPlaceHandler;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;


@Getter @Setter @RequiredArgsConstructor @ToString
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

    // 建筑基点位置
    private Location spawnLocation;

    // 建筑结构体
    private COIStructure structure;

    // 是否建造空气方块
    private boolean withAir = true;

    // 小队的特殊颜色方块替换
    private Material blockColor;

    // 建筑物生成的NPC创建工具
    private List<COINpc> npcCreators;

    // 替换方块时触发
    private BlockPlaceHandler handler;

    // 方块替换的条件
    private BlockPlaceCondition condition;

    public COIPaster(boolean complete, int unit, long interval, String worldName, Location location, Location spawnLocation, COIStructure structure, boolean withAir, Material blockColor, List<COINpc> npcCreators, BlockPlaceHandler handler) {
        this.complete = complete;
        this.unit = unit;
        this.interval = interval;
        this.worldName = worldName;
        this.location = location;
        this.spawnLocation = spawnLocation;
        this.structure = structure;
        this.withAir = withAir;
        this.blockColor = blockColor;
        this.npcCreators = npcCreators;
        this.handler = handler;
        this.condition = (b, coiBlock, material) -> true;
    }
}
