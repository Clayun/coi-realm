package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.tools.building.LineBuild;
import com.mcylm.coi.realm.tools.data.BuildData;
import com.mcylm.coi.realm.utils.BuildingUtils;
import org.bukkit.block.Block;

public class COIWall extends LineBuild {

    public COIWall() {
        initStructure();
        setType(COIBuildingType.WALL_NORMAL);
        setLevel(1);
        setMaxLevel(1);
        setConsume(16);
        setAvailable(true);
    }

    @Override
    public LineBuild cloneBuild() {
        COIWall wall = new COIWall();
        wall.setTeam(getTeam());
        return wall;
    }

    @Override
    public boolean pointCheck(Block block) {
        return BuildData.getBuildingByBlock(block) == null;
    }

    private void initStructure(){
        getBuildingLevelStructure().put(1,"wall1.structure");

    }
}
