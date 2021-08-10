package com.mcylm.coi.realm.listener;

import com.mcylm.coi.realm.cache.PlayerClipboard;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {

    /**
     * 粘贴板选取点
     * @param event
     */
    @EventHandler
    public void onClipboardSelectPoint(PlayerInteractEvent event){

        Action action = event.getAction();
        //判断是右手，同时避免触发两次
        if(Action.RIGHT_CLICK_BLOCK  == action && event.getHand().equals(EquipmentSlot.HAND)){


            Block clickedBlock = event.getClickedBlock();
            Location location = clickedBlock.getLocation();
            Player player = event.getPlayer();

            PlayerClipboard.point(player,location);

        }

    }
}
