package com.mcylm.coi.realm.listener;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class MineralsBreakListener implements Listener {

    @EventHandler
    public void onMineralsBreak(BlockBreakEvent event){

        List<String> blockMaterials = Entry.getInstance().getConfig().getStringList("miner.breaks");

        if(blockMaterials != null
            && !blockMaterials.isEmpty()){

            // 判断是否属于矿工挖掘的方块类型
            for(String materialName : blockMaterials){
                Material material = Material.getMaterial(materialName);

                if(event.getBlock().getType().equals(material)){

                    Block block = event.getBlock();

                    COIBlock coiBlock = new COIBlock();
                    coiBlock.setX(block.getX());
                    coiBlock.setY(block.getY());
                    coiBlock.setZ(block.getZ());
                    coiBlock.setMaterial(block.getType().name());
                    coiBlock.setBlockData(block.getBlockData().getAsString());

                    Location location = block.getLocation();


                    // 重生矿物方块
                    new BukkitRunnable() {
                        @Override
                        public void run() {

                            Material material = Material.getMaterial(coiBlock.getMaterial());

                            BlockData blockData = Bukkit.createBlockData(coiBlock.getBlockData());

                            block.setType(material);

                            BlockState state = block.getState();
                            state.setBlockData(blockData);
                            state.update(true);

                            LoggerUtils.debug("方块重生："+coiBlock.getMaterial());

                        }
                    }.runTaskLater(Entry.getInstance(),20 * 5L);
                }

            }


        }


    }
}
