package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.ConnectableBuild;
import com.mcylm.coi.realm.tools.building.data.BuildData;
import com.mcylm.coi.realm.utils.BuildingUtils;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class COIWall extends ConnectableBuild {

    public COIWall() {
        setType(COIBuildingType.WALL_NORMAL);
        setLevel(1);
        setMaxLevel(2);
        setConsume(12);
        setAvailable(true);
    }

    @Override
    public void build(Location location, Player player) {
        super.build(location, player);
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                buildWall(location.clone().add(x,0,z), -5, 8);
            }
        }
        buildSuccess(location, player);
    }

    @Override
    public void buildPoint(Location point, Vector line) {
        buildWall(point, -5, 6);
    }

    @Override
    public boolean connectConditionsCheck(ConnectableBuild to) {

        return (getType() == COIBuildingType.WALL_NORMAL || getType() == COIBuildingType.DOOR_NORMAL) && super.connectConditionsCheck(to);
    }

    @Override
    public int getMaxConnectBuild() {
        return 4;
    }

    public void buildWall(Location location, int minY, int maxY) {
        for (int y = minY; y < maxY; y++) {
            Block block = location.clone().add(0,y,0).getBlock();
            if (!block.isSolid()) {
                COIBuilder.placeBlockForBuilding(block, this, Material.STONE);
            }
        }
    }

    @Override
    public int getMaxHealth() {
        return 200 + getLevel() * 100;
    }
}
