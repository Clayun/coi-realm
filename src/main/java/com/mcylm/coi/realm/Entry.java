package com.mcylm.coi.realm;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class Entry extends ExtendedJavaPlugin {

    private static Entry instance;

    public static String PREFIX = "岛屿冲突领域";

    public static String PLUGIN_FILE_PATH = "plugins/"+ Entry.getInstance().getName()+"/";

    public static Entry getInstance() {
        return instance;
    }

    @Override
    protected void enable() {

        instance = this;

        saveDefaultConfig();

        Block blockAt = Bukkit.getWorld("").getBlockAt(1, 1, 1);

        final Block block = blockAt;
        Material material = Material.getMaterial("diorite");

        block.setType(material);
        block.setBlockData(Bukkit.createBlockData("minecraft:diorite"));
        block.getState().update(true);

    }

    @Override
    protected void disable() {

    }

}
