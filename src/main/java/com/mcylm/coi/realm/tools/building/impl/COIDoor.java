package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.utils.TeamUtils;
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

@Setter
@Getter
public class COIDoor extends COIBuilding {

    private Set<Block> doorBlocks = new HashSet<>();

    // 当前门是否是开着的
    private boolean open = false;
    private Material doorMaterial;

    public COIDoor() {
        setLevel(1);
        setDoorMaterial(Material.IRON_BLOCK);
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

        for (Block b : getBlocks()) {
            if (b.getType() == doorMaterial) {
                doorBlocks.add(b);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isAlive()) {
                    this.cancel();
                    return;
                }

                boolean openDoor = false;
                for (Player p : Entry.getInstance().getServer().getOnlinePlayers()) {

                    if(!p.getWorld().getName().equals(Entry.WORLD)){
                        TeamUtils.tpSpawner(p);
                    }

                    if (TeamUtils.inTeam(p.getName(),getTeam()) && p.getLocation().distance(location) <= 6) {
                        openDoor = true;
                    }
                }
                if (openDoor && !isOpen()) {
                    Bukkit.getScheduler().runTask(Entry.getInstance(), () -> open());
                } else if (!openDoor && isOpen()) {
                    Bukkit.getScheduler().runTask(Entry.getInstance(), () -> close());
                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 1, 20);

    }

    @Override
    public int getMaxHealth() {
        return 400 + getLevel() * 200;
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
