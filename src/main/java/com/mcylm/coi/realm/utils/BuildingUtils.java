package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.tools.building.impl.COIBuilding;
import com.mcylm.coi.realm.tools.building.impl.COIMill;
import com.mcylm.coi.realm.tools.building.impl.COIStope;

import java.util.ArrayList;
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

        buildings.add(mill);
        buildings.add(stope);

        return buildings;

    }

}
