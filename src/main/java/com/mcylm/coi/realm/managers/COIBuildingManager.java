package com.mcylm.coi.realm.managers;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class COIBuildingManager {

    private final Map<COIBuildingType, Class<? extends COIBuilding>> buildingTypeClassMap = new HashMap<>();
    private final List<Class<? extends COIBuilding>> buildingOrderList = new ArrayList<>();

    private final Map<COIBuildingType, BuildingConfig> buildingConfigMap = new HashMap<>();

    // TODO: Reload 命令
    public void registerBuilding(COIBuildingType type, Class<? extends COIBuilding> clazz) {
        buildingTypeClassMap.put(type, clazz);
        buildingOrderList.add(clazz);

        File folder = new File(Entry.getInstance().getDataFolder(), "buildings");
        folder.mkdirs();
        File configFile = new File(folder, type.getCode().toLowerCase() + ".json");

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                BuildingConfig config = Entry.GSON.fromJson(reader, BuildingConfig.class);
                buildingConfigMap.put(type, config);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                BuildingConfig config = getBuildingTemplateByType(type).getDefaultConfig();
                buildingConfigMap.put(type, config);

                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write(Entry.GSON.toJson(config));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void unregisterBuilding(COIBuildingType type) {

        buildingOrderList.removeIf(c -> c.equals(buildingTypeClassMap.get(type)));
        buildingTypeClassMap.remove(type);
        buildingConfigMap.remove(type);
    }

    public COIBuilding getBuildingTemplateByType(COIBuildingType type) throws Exception {
        if (!buildingTypeClassMap.containsKey(type)) {
            throw new Exception("没有为类型找到对应的模板: " + type.getName());
        }
        Class<? extends COIBuilding> clazz = buildingTypeClassMap.get(type);
        COIBuilding building = clazz.getDeclaredConstructor().newInstance();
        building.setType(type);
        if (buildingConfigMap.containsKey(type)) building.setConfig(buildingConfigMap.get(type));

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
                building.setConfig(buildingConfigMap.get(type));
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
