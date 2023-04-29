package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.impl.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 建筑工具类
 */
public class BuildingUtils {

    /**
     * 获取所有建筑模板
     * @return
     */
    public static List<COIBuilding> getBuildingsTemplate(){

        List<COIBuilding> buildings = new ArrayList<>();

        COIStope stope = new COIStope();
        COIMill mill = new COIMill();
        COICamp camp = new COICamp();
        COIWall wall = new COIWall();
        COIDoor door = new COIDoor();

        buildings.add(mill);
        buildings.add(stope);
        buildings.add(camp);
        buildings.add(wall);
        buildings.add(door);

        return buildings;

    }

}
