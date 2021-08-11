package com.mcylm.coi.realm.listener;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.cache.PlayerClipboard;
import com.mcylm.coi.realm.enums.COIServerMode;
import com.mcylm.coi.realm.tools.COIBuilder;
import com.mcylm.coi.realm.tools.COIPaster;
import com.mcylm.coi.realm.tools.COIStructure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {

    /**
     * 开发维护环境使用
     * 粘贴板选取点
     * @param event
     */
    @EventHandler
    public void onClipboardSelectPoint(PlayerInteractEvent event){

        Action action = event.getAction();

        //开发维护环境才启用
        if(COIServerMode.DEVELOP.getCode().equals(Entry.SERVER_MODE)){

            //判断是右手，同时避免触发两次
            if(Action.RIGHT_CLICK_BLOCK  == action && event.getHand().equals(EquipmentSlot.HAND)
                    //必须是OP
                    && event.getPlayer().isOp()
                    //钻石斧
                    && event.getPlayer().getEquipment().getItemInMainHand().getType() == Material.DIAMOND_AXE){

                Block clickedBlock = event.getClickedBlock();
                Location location = clickedBlock.getLocation();
                Player player = event.getPlayer();

                PlayerClipboard.point(player,location);

            }

        }


    }

    /**
     * 粘贴建筑
     * @param event
     */
    @EventHandler
    public void onPasteBuilding(PlayerInteractEvent event){

        Action action = event.getAction();

        //判断是右手，同时避免触发两次
        if(Action.RIGHT_CLICK_BLOCK  == action && event.getHand().equals(EquipmentSlot.HAND)
                //空手触发
                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.DIAMOND_PICKAXE){

            Block clickedBlock = event.getClickedBlock();
            Location location = clickedBlock.getLocation();
            Player player = event.getPlayer();

            //测试使用
            COIStructure newTest = COIBuilder.getStructureByFile("newTest.structure");

            //创建一个粘贴工具
            COIPaster coiPaster = new COIPaster(1,10,player.getWorld().getName(),location,newTest,false);

            //更新世界方块
            COIBuilder.pasteStructure(coiPaster,player);

        }

    }
}
