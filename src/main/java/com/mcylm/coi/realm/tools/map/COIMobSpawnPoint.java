package com.mcylm.coi.realm.tools.map;

import com.google.gson.annotations.Expose;
import com.mcylm.coi.realm.tools.handler.SpawnHandler;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;
import java.util.Random;

@Setter
@Getter
@NoArgsConstructor
public class COIMobSpawnPoint {

    public static Random random = new Random();

    private int x;
    private int y;
    private int z;
    private String world;
    private int maxRadius;

    @Expose(deserialize = false, serialize = false)
    public List<SpawnHandler> handlers;

    public COIMobSpawnPoint(int x, int y, int z, String world, int maxRadius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.maxRadius = maxRadius;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public void spawn(int second) {

        Location location = getLocation();

        for (SpawnHandler handler : handlers) {
            handler.spawn(second, location.getWorld().getHighestBlockAt(location.getBlockX() + random.nextInt(-maxRadius, maxRadius), location.getBlockZ() + random.nextInt(-maxRadius, maxRadius)).getLocation());
        }

    }

}
