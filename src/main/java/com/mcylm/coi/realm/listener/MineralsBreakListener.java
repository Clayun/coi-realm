package com.mcylm.coi.realm.listener;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

    /**
     * 玩家在游戏中挖矿的行为
     * @param event
     */
    @EventHandler
    public void onMineralsBreak(BlockBreakEvent event){

        // 创造模式可以随便搞
        if(event.getPlayer().getGameMode().equals(GameMode.CREATIVE)){
            // 创造模式
            event.setCancelled(false);
            return;
        }

        if(Entry.getGame().getStatus().equals(COIGameStatus.GAMING)){
            // 如果是游戏中

            // 本方法处理的是玩家挖矿的逻辑,NPC挖矿的逻辑在NPC类里
            if(null != TeamUtils.getTeamByPlayer(event.getPlayer())){
                List<String> blockMaterials = Entry.getInstance().getConfig().getStringList("miner.breaks");
                // 方块复活时间
                int restoreTimer = Entry.getInstance().getConfig().getInt("game.mineral-restore-timer");

                if(!blockMaterials.isEmpty()){

                    boolean matched = false;

                    // 判断是否属于矿工挖掘的方块类型
                    for(String materialName : blockMaterials){
                        Material material = Material.getMaterial(materialName);

                        if(event.getBlock().getType().equals(material)){
                            matched = true;

                            Block block = event.getBlock();

                            COIBlock coiBlock = new COIBlock();
                            coiBlock.setX(block.getX());
                            coiBlock.setY(block.getY());
                            coiBlock.setZ(block.getZ());
                            coiBlock.setMaterial(block.getType().name());
                            coiBlock.setBlockData(block.getBlockData().getAsString());

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
                            }.runTaskLater(Entry.getInstance(),20L * restoreTimer);
                        }

                    }

                    if(!matched){
                        // 如果是非游戏允许挖掘的矿物，就禁止破坏方块
                        event.setCancelled(true);
                    }


                }
            }
        }else
            // 如果是非游戏模式下，禁止破坏方块
            event.setCancelled(true);

    }
}
