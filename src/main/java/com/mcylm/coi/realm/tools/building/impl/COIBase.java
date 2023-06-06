package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.tools.npc.impl.COIFarmer;
import com.mcylm.coi.realm.utils.GUIUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                .setConsume(200);
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
