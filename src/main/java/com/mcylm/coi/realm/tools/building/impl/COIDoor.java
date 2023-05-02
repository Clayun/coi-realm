package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.model.COIStructure;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.ConnectableBuild;
import com.mcylm.coi.realm.tools.data.BuildData;
import com.mcylm.coi.realm.utils.BuildingUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import com.mcylm.coi.realm.utils.rotation.Rotation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class COIDoor extends COIBuilding {

    private Set<Block> doorBlocks = new HashSet<>();
    //private List<Location> connectPoints = new ArrayList<>();
    private boolean open;
    private Material doorMaterial;
    //private Material connectPointMaterial;


    public COIDoor() {

        setType(COIBuildingType.DOOR_NORMAL);
        setLevel(1);
        setMaxLevel(2);
        initStructure();
        setConsume(12);
        //setDoorMaterial(Material.IRON_BLOCK);
        //setConnectPointMaterial(Material.REDSTONE_BLOCK);
        setAvailable(true);

    }



    @Override
    public void buildSuccess(Location location, Player player) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isAlive()) {
                    this.cancel();
                    return;
                }

                boolean openDoor = false;
                for (Player p : location.getWorld().getPlayers()) {
                    if (TeamUtils.getTeamByPlayer(p) == getTeam() && p.getLocation().distance(location) <= 6) {
                        openDoor = true;
                    }
                }
                if (openDoor && !isOpen()) {
                    Bukkit.getScheduler().runTask(Entry.getInstance(), () -> open());
                } else if (!openDoor && isOpen()) {
                    Bukkit.getScheduler().runTask(Entry.getInstance(), () -> close());
                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 1, 1);

        for (Block b : getBlocks()) {
            if (b.getType() == doorMaterial) {
                doorBlocks.add(b);
            }
        }
        super.buildSuccess(location, player);
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

    public void open() {
        if (!isAlive()) return;
        this.open = true;
        doorBlocks.forEach(b -> b.setType(Material.AIR));
    }

    public void close() {
        if (!isAlive()) return;
        this.open = false;
        doorBlocks.forEach(b -> b.setType(doorMaterial));
    }




    private void initStructure(){
        getBuildingLevelStructure().put(1,"door1.structure");
        getBuildingLevelStructure().put(2,"door2.structure");
    }
}
