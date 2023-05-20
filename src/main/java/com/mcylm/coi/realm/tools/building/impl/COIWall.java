package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.LineBuild;
import com.mcylm.coi.realm.tools.data.BuildData;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public class COIWall extends LineBuild {

    public COIWall() {
        initStructure();
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
        @Nullable COIBuilding building = BuildData.getBuildingByBlock(block);

        if (building == null) {
            return true;
        }
        if (building.getType() == COIBuildingType.WALL_NORMAL) {
            return true;
        }
        return false;
    }

    private void initStructure(){
        getBuildingLevelStructure().put(1,"wall1.structure");

    }
}
