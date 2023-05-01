package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.BuildData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.*;

public class LocationUtils {
    private static final BlockFace[] BLOCK_FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    private static <T> int indexOf(T[] arr, T key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }
    public static List<Location> line(Location locAO, Location locBO, double rate) {
        Location locA = locAO.clone();
        Location locB = locBO.clone();
        rate = Math.abs(rate);
        Vector vectorAB = locB.clone().subtract(locA).toVector();
        double vectorLength = vectorAB.length();
        vectorAB.normalize();
        List<Location> points = new ArrayList<>();
        for (double i = 0; i < vectorLength; i += rate) {
            Vector vector = vectorAB.clone().multiply(i);
            locA.add(vector);
            points.add(locA.clone());
            locA.subtract(vector);
        }

        return points;
    }


    public static Set<Block> selectionRadius(Block firstlyBrokeCentralBlock, int horizontalRadius, int verticalHeight) {
        Set<Block> blocks = new HashSet<>();
        Location blockLocation = firstlyBrokeCentralBlock.getLocation();
        for(int x = blockLocation.getBlockX() - horizontalRadius;  x <= blockLocation.getBlockX() + horizontalRadius; x++) {
            for(int y = blockLocation.getBlockY() - verticalHeight; y <= blockLocation.getBlockY() + verticalHeight; y++) {
                for(int z = blockLocation.getBlockZ() - horizontalRadius; z <= blockLocation.getBlockZ() + horizontalRadius; z++) {
                    Block block = firstlyBrokeCentralBlock.getWorld().getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) blocks.add(block);
                }
            }
        }
        return blocks;
    }
    public static List<COIBuilding>getNearbyBuildings(Location location, int radius) {
        List<COIBuilding> list = new ArrayList<>();
        for (Block block : selectionRadius(location.getBlock(), radius, radius)) {
            COIBuilding building = BuildData.getBuildingByBlock(block);
            if (building != null && !list.contains(building)) list.add(building);
        }
        list.sort(Comparator.comparingDouble(s -> s.getLocation().distance(location)));

        return list;
    }
    public static BlockFace rotateBlockFace(BlockFace face, int rotation, boolean mirror) {
        if (face == BlockFace.DOWN || face == BlockFace.UP || face == BlockFace.SELF) {
            return face;
        }
        rotation %= 4;
        if (rotation < 0) {
            rotation += 4;
        }
        int ind = indexOf(BLOCK_FACES, face);
        if (ind == -1) {
            return face;
        }
        if (mirror && (ind == 1 || ind == 3)) {
            ind = ind + 2;
        }
        ind = (ind + rotation) % 4;
        return BLOCK_FACES[ind];
    }

}
