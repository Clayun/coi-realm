package com.mcylm.coi.realm.listener;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.clipboard.PlayerClipboard;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIServerMode;
import com.mcylm.coi.realm.events.BuildingTouchEvent;
import com.mcylm.coi.realm.gui.BuildEditGUI;
import com.mcylm.coi.realm.gui.BuilderGUI;
import com.mcylm.coi.realm.gui.ChooseTeamGUI;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.impl.COIRepair;
import com.mcylm.coi.realm.tools.building.impl.COITurret;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.tools.npc.impl.COIMiner;
import com.mcylm.coi.realm.tools.selection.Selector;
import com.mcylm.coi.realm.utils.GUIUtils;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @EventHandler
    public void onChooseTeam(PlayerInteractEvent event) {
        if (event.getHand().equals(EquipmentSlot.HAND)
                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.COMPASS
                && ItemUtils.getName(event.getPlayer().getInventory().getItemInMainHand()).equals(LoggerUtils.replaceColor("&c选择队伍"))
        ) {

            // 选队伍
            new ChooseTeamGUI(event.getPlayer()).open();
        }
    }


    /**
     * 粘贴建筑并设置NPC
     * @param event
     */
    @EventHandler
    public void onBuilding(PlayerInteractEvent event) {

        Action action = event.getAction();

        //判断是右手，同时避免触发两次
        if (Action.RIGHT_CLICK_BLOCK == action && event.getHand().equals(EquipmentSlot.HAND)
                //空手触发
                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.BOOK
                && ItemUtils.getName(event.getPlayer().getInventory().getItemInMainHand()).equals(LoggerUtils.replaceColor("&b建筑蓝图"))
        ) {

            Block clickedBlock = event.getClickedBlock();
            Location location = clickedBlock.getLocation();

            COIBuilding building = BuildData.getBuildingByBlock(clickedBlock);
            if (building != null) {

                if (building.getTeam() == TeamUtils.getTeamByPlayer(event.getPlayer())) {
                    new BuildEditGUI(event.getPlayer(), building).open();
                }
            } else {

                Player player = event.getPlayer();

                if (Selector.selectors.containsKey(player)) {
                    Selector.selectors.get(player).selectLocation(location);
                } else {
                    new BuilderGUI(player, location);
                }
            }
        }

        if (event.getClickedBlock() != null) {
            @Nullable COIBuilding building = BuildData.getBuildingByBlock(event.getClickedBlock());
            if (building != null) {
                // 触发建筑按钮事件
                BuildingTouchEvent touchEvent = new BuildingTouchEvent(building,event.getPlayer());
                Bukkit.getServer().getPluginManager().callEvent(touchEvent);

                // 原本的事件监听转移到了GameListener
            }
        }

    }

    @EventHandler
    public void onToggleItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (Selector.selectors.containsKey(player)) {
            Selector.selectors.get(player).stop(true);

        }
    }

//    @EventHandler
    public void onMinerSpawn(PlayerInteractEvent event){

        Action action = event.getAction();

        //判断是右手，同时避免触发两次
        if(Action.RIGHT_CLICK_BLOCK  == action && event.getHand().equals(EquipmentSlot.HAND)
                //空手触发
                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.DIAMOND_HOE){

            Block clickedBlock = event.getClickedBlock();
            Location location = clickedBlock.getLocation();

            //背包内的物品
            List<ItemStack> inventory = new ArrayList<>();
            ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
            inventory.add(pickaxe);
            ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
            inventory.add(boots);


            Set<String> breakBlockMaterials = new HashSet<>();
            breakBlockMaterials.add("SAND");

            Set<String> pickItemMaterials = new HashSet<>();
            pickItemMaterials.add("SAND");
            pickItemMaterials.add("APPLE");
            pickItemMaterials.add("BREAD");

            Location chestLocation = new Location(event.getPlayer().getWorld(), 293.506d, 72.875d, 30.637d);

            List<Location> locations = new ArrayList<>();
            locations.add(chestLocation);

            COIMinerCreator coiNpc = new COIMinerCreator(locations);
            coiNpc.setInventory(GUIUtils.createNpcInventory(3));
            coiNpc.setAggressive(false);
            coiNpc.setAlertRadius(5);
            coiNpc.setBreakBlockMaterials(breakBlockMaterials);
            coiNpc.setName("矿工");
            coiNpc.setLevel(1);
            coiNpc.setPickItemMaterials(pickItemMaterials);
            coiNpc.setSkinName("solider");
            coiNpc.setSkinSignature("LwMy/g2xAdhfHErWkk6pMM7SnIa2ERQW5X64w1q+/eEW3aamwP/1//nBdUqlWDZb/bQ0zhsl" +
                    "/JmnnJ118ePKzS6p7Gs1Hbk70EVEkuGA2f5VUK4F868944GHGxZAhbSC766IMSGuUCiusRfxuXHsF8k0LqKWZbO+" +
                    "enG46hS+V/T81F7HvDm+rOOxpbwCByghLHcAwiKNQTDWzQD+tIkaUI8hHP2MF4RMzih4rMmD1AteAa3vKjNE5cKyk" +
                    "bRsRfwL6p6LQzOCCSB5aJe8eLOErCBVN7E0xBHVIpNm3CoEVf4IG/rvZf/pgx8g39gsD6E4Gdqw5OrgVSCj63nQrapF" +
                    "WXTNqvEz6PdLd6hiagqPtIzujvHaVKVoJFC34X+0SGG6N9APnFx4ATW0HSmKuGsgVhvA03w6x0uyHCchbcG6lVRDEiWsNx" +
                    "Wf11BFsOchFCqRyZK5hVLoSP3SWyBXTCNAHVhHzhVxl1EpGSpEZtB9kLWcl9XrLc3ykT16gy9p0WYH38HtwILVTmm88gXhh" +
                    "vTRl+hG+WDdZbk2VyUAmVyD0g9semGkn1v00in8SdjtMi+ATV2Ej0RTPgJJ/m/qwpWLQJF5ru/mWaXAq5UTqaCKFauEWNa" +
                    "6+Tr4AqNAOtrtQVgspk/N9tWDUdKuxY7FuU9GFrbBB7aTrRSQka7WQzeaKtA=");
            coiNpc.setSkinTextures("eyJ0aW1lc3RhbXAiOjE1NzY1MDk3M" +
                    "Dc0MTcsInByb2ZpbGVJZCI6ImZkNjBmMzZmNTg2MTRmMTJiM2NkNDdjMmQ4NTUyOTlhI" +
                    "iwicHJvZmlsZU5hbWUiOiJSZWFkIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzI" +
                    "jp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NDFlZ" +
                    "WQ5OTU5MGRkMGRlZjE0MjhiODJhMmE4OTA3OTczN2Q3ZjVhZDA4MTQ5MTVlZmY1ZDdmNjgyNTk2OWYzIn19fQ");

            coiNpc.setSpawnLocation(location);



            COIMiner worker = new COIMiner(coiNpc);

            worker.spawn(location);

            new BukkitRunnable() {
                @Override
                public void run() {
                   worker.move();
                }
            }.runTaskTimer(Entry.getInstance(),0,20l);
        }

    }

}
