package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.FloatableBuild;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

@Data
public class COIBridge extends FloatableBuild {

    public COIBridge() {
        setLevel(1);
        setAvailable(true);
        initStructure();
    }

    @Override
    public BuildingConfig getDefaultConfig() {
        return new BuildingConfig()
                .setConsume(32)
                .setMaxLevel(2)
                .setMaxBuild(9999)
                .setStructures(getBuildingLevelStructure());
    }

    @Override
    public void buildSuccess(Location location, Player player) {
        super.buildSuccess(location, player);
    }

    @Override
    public int getMaxHealth() {
        return 200 + getLevel() * 50;
    }

    private void initStructure(){
        getBuildingLevelStructure().put(1,"qiao1.structure");
        getBuildingLevelStructure().put(2,"qiao1.structure");
    }
}
