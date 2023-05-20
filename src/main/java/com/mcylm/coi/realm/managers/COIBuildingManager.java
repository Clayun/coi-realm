package com.mcylm.coi.realm.managers;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.tools.building.COIBuilding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class COIBuildingManager {

    private final Map<COIBuildingType, Class<? extends COIBuilding>> buildingTypeClassMap = new HashMap<>();
    private final List<Class<? extends COIBuilding>> buildingOrderList = new ArrayList<>();

    public void registerBuilding(COIBuildingType type, Class<? extends COIBuilding> clazz) {
        buildingTypeClassMap.put(type, clazz);
        buildingOrderList.add(clazz);
    }

    public void unregisterBuilding(Class<? extends COIBuilding> clazz) {
        buildingTypeClassMap.values().removeIf(c -> c.equals(clazz));
        buildingOrderList.removeIf(c -> c.equals(clazz));
    }

    public COIBuilding getBuildingTemplateByType(COIBuildingType type) throws Exception {
        if (!buildingTypeClassMap.containsKey(type)) {
            throw new Exception("没有为类型找到对应的模板: " + type.getName());
        }
        Class<? extends COIBuilding> clazz = buildingTypeClassMap.get(type);
        COIBuilding building = clazz.getDeclaredConstructor().newInstance();
        building.setType(type);
        return building;
    }

    public List<COIBuilding> getAllBuildingTemplates() {
        List<COIBuilding> result = new ArrayList<>();
        for (Class<? extends COIBuilding> clazz : buildingOrderList) {
            COIBuildingType type;
            try {
                type = getBuildingTypeFromClass(clazz);
                COIBuilding building = clazz.getDeclaredConstructor().newInstance();
                building.setType(type);
            result.add(building);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private COIBuildingType getBuildingTypeFromClass(Class<? extends COIBuilding> clazz) throws Exception {
        for (Map.Entry<COIBuildingType, Class<? extends COIBuilding>> entry : buildingTypeClassMap.entrySet()) {
            if (entry.getValue().equals(clazz)) {
                return entry.getKey();
            }
        }
        throw new Exception("没有为类找到对应的类型: " + clazz.getSimpleName());
    }

}
