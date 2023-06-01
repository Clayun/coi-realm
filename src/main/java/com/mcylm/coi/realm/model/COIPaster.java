package com.mcylm.coi.realm.model;

import com.mcylm.coi.realm.tools.building.handlers.BlockPlaceHandler;
import com.mcylm.coi.realm.tools.npc.impl.COIHuman;
import lombok.AllArgsConstructor;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;
import java.util.concurrent.Callable;


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

    // 防御塔类建筑的发射方块位置
    private Location muzzle;

    // 建筑结构体
    private COIStructure structure;

    // 是否建造空气方块
    private boolean withAir;

    // 小队的特殊颜色方块替换
    private Material blockColor;

    // 建筑物生成的NPC创建工具
    private List<COINpc> npcCreators;

    // 替换方块时触发
    private BlockPlaceHandler handler;

    public COIPaster(boolean complete, int unit, long interval, String worldName, Location location, COIStructure structure, boolean withAir, Material blockColor, List<COINpc> npcCreators) {
        this.complete = complete;
        this.unit = unit;
        this.interval = interval;
        this.worldName = worldName;
        this.location = location;
        this.structure = structure;
        this.withAir = withAir;
        this.blockColor = blockColor;
        this.npcCreators = npcCreators;
        this.handler = ((block, blockToPlace, type) -> Material.valueOf(blockToPlace.getMaterial()));
    }
}
