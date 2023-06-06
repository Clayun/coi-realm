package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * 基地
 */
public class COIBase extends COIBuilding {

    public COIBase() {
        // 默认等级为1
        setLevel(1);
        // 初始化NPC创建器
        setNpcCreators(new ArrayList<>());
        //初始化完成，可建造
        setAvailable(true);
        initStructure();
    }

    @Override
    public BuildingConfig getDefaultConfig() {

        return new BuildingConfig()
                .setStructures(getBuildingLevelStructure())
                .setMaxLevel(10)
                .setShowInMenu(false)
                .setConsume(200);
    }

    @Override
    public void buildSuccess(Location location, Player player) {


    }

    @Override
    public void upgradeBuildSuccess() {
        super.upgradeBuildSuccess();

    }

    @Override
    public void upgradeBuild(Player player) {
        super.upgradeBuild(player);
    }

    private void initStructure() {
        getBuildingLevelStructure().put(1, "base1.structure");
        getBuildingLevelStructure().put(2, "base2.structure");
        getBuildingLevelStructure().put(3, "base2.structure");
        getBuildingLevelStructure().put(4, "base2.structure");
        getBuildingLevelStructure().put(5, "base2.structure");
        getBuildingLevelStructure().put(6, "base2.structure");
        getBuildingLevelStructure().put(7, "base2.structure");
        getBuildingLevelStructure().put(8, "base2.structure");
        getBuildingLevelStructure().put(9, "base2.structure");
        getBuildingLevelStructure().put(10, "base2.structure");
    }

    @Override
    public int getMaxHealth() {
        return 1000 + getLevel() * 1000;
    }
}
