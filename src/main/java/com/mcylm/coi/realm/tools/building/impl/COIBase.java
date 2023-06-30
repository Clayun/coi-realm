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
                .setMaxLevel(25)
                .setMaxBuild(1)
                .setShowInMenu(false)
                .setConsume(128);
    }

    @Override
    public void buildSuccess(Location location, Player player) {
        // 生成之后，把基地的箱子设置为资源共享箱子
        // 矿车会自动把物资运到这些箱子当中
        getTeam().setResourcesChests(getChestsLocation());
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

        for(int i = 0;i<=100;i++){
            getBuildingLevelStructure().put(i, "base1.structure");
        }
    }

    @Override
    public int getMaxHealth() {
        return 500 + getLevel() * 500;
    }
}
