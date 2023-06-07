package com.mcylm.coi.realm.tools.map;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COIPaster;
import com.mcylm.coi.realm.model.COIStructure;
import com.mcylm.coi.realm.utils.rotation.Rotation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class COIVein {

    private String structureName;
    private int x;
    private int y;
    private int z;
    private float yaw;

    private String world;
    @Getter
    private double spawnChance;

    private int restTime;

    public BukkitRunnable startGenerate() {

        Location location = getLocation();
        @NotNull BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {

                placeVein(location);

            }
        };
        runnable.runTaskTimer(Entry.getInstance(), 0, restTime * 20L);
        return runnable;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public void placeVein(Location location) {
        COIStructure structure = Entry.getBuilder().getStructureByFile(structureName);
        structure.rotate(Rotation.fromDegrees(Math.round(yaw / 90) * 90));
        Entry.getBuilder().pasteStructureWithoutBuilding(new COIPaster(false, 5, 5, world, location, null, null , structure, false, Material.STONE, List.of(), ((block, blockToPlace, material) -> material)), null);
    }


}
