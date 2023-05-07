package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.tools.npc.impl.COISoldier;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 军营
 * 可以生成战士
 */
public class COICamp extends COIBuilding {

    public COICamp() {
        // 设置建筑类型为军营
        setType(COIBuildingType.MILITARY_CAMP);
        // 默认等级为1
        setLevel(1);
        // 最高等级为2级
        setMaxLevel(2);
        // 设置等级对应的建筑文件
        initStructure();
        // 初始化NPC创建器
        setNpcCreators(List.of(initSoldierCreator(), initSoldierCreator(), initSoldierCreator()));
        // 军营设置所需消耗的材料
        setConsume(128);
        //初始化完成，可建造
        setAvailable(true);
    }

    @Override
    public void build(Location location, Player player){
        super.build(location,player);

            new BukkitRunnable() {

                COISoldier soldier = null;

                @Override
                public void run() {

                    // 如果建筑建造完成，NPC就初始化
                    if(isComplete()){
                        for (COINpc creator : getNpcCreators()) {
                            COISoldierCreator npcCreator = (COISoldierCreator) creator;

                            // 设置NPC跟随创造建筑的玩家
                            npcCreator.setFollowPlayerName(player.getName());
                            // 初始化
                            soldier = new COISoldier(npcCreator);
                            soldier.spawn(creator.getSpawnLocation());

                            // 关闭Ticker
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(Entry.getInstance(),0,20l);


    }

    /**
     * 构造一个战士NPC创建器
     * @return
     */
    private COISoldierCreator initSoldierCreator(){

        // 背包内的物品
        List<ItemStack> inventory = new ArrayList<>();

        // 铁剑
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        inventory.add(sword);
        ItemStack bow = new ItemStack(Material.BOW);
        inventory.add(sword);

        // 不破坏方块
        Set<String> breakBlockMaterials = new HashSet<>();

        // 捡起的东西
        Set<String> pickItemMaterials = new HashSet<>();
        pickItemMaterials.add("APPLE");
        pickItemMaterials.add("BREAD");

        // 将装备默认设为捡起
        List<Material> clothes = COINpc.CLOTHES;
        for(Material clothesType : clothes){
            pickItemMaterials.add(clothesType.name());
        }

        COISoldierCreator npcCreator = new COISoldierCreator();
        npcCreator.setInventory(inventory);
        npcCreator.setAggressive(true);
        npcCreator.setAlertRadius(5);
        npcCreator.setBreakBlockMaterials(breakBlockMaterials);
        npcCreator.setName("战士");
        npcCreator.setLevel(1);
        npcCreator.setPickItemMaterials(pickItemMaterials);
        npcCreator.setSkinName("solider");
        npcCreator.setSkinSignature("LwMy/g2xAdhfHErWkk6pMM7SnIa2ERQW5X64w1q+/eEW3aamwP/1//nBdUqlWDZb/bQ0zhsl" +
                "/JmnnJ118ePKzS6p7Gs1Hbk70EVEkuGA2f5VUK4F868944GHGxZAhbSC766IMSGuUCiusRfxuXHsF8k0LqKWZbO+" +
                "enG46hS+V/T81F7HvDm+rOOxpbwCByghLHcAwiKNQTDWzQD+tIkaUI8hHP2MF4RMzih4rMmD1AteAa3vKjNE5cKyk" +
                "bRsRfwL6p6LQzOCCSB5aJe8eLOErCBVN7E0xBHVIpNm3CoEVf4IG/rvZf/pgx8g39gsD6E4Gdqw5OrgVSCj63nQrapF" +
                "WXTNqvEz6PdLd6hiagqPtIzujvHaVKVoJFC34X+0SGG6N9APnFx4ATW0HSmKuGsgVhvA03w6x0uyHCchbcG6lVRDEiWsNx" +
                "Wf11BFsOchFCqRyZK5hVLoSP3SWyBXTCNAHVhHzhVxl1EpGSpEZtB9kLWcl9XrLc3ykT16gy9p0WYH38HtwILVTmm88gXhh" +
                "vTRl+hG+WDdZbk2VyUAmVyD0g9semGkn1v00in8SdjtMi+ATV2Ej0RTPgJJ/m/qwpWLQJF5ru/mWaXAq5UTqaCKFauEWNa" +
                "6+Tr4AqNAOtrtQVgspk/N9tWDUdKuxY7FuU9GFrbBB7aTrRSQka7WQzeaKtA=");
        npcCreator.setSkinTextures("eyJ0aW1lc3RhbXAiOjE1NzY1MDk3M" +
                "Dc0MTcsInByb2ZpbGVJZCI6ImZkNjBmMzZmNTg2MTRmMTJiM2NkNDdjMmQ4NTUyOTlhI" +
                "iwicHJvZmlsZU5hbWUiOiJSZWFkIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzI" +
                "jp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NDFlZ" +
                "WQ5OTU5MGRkMGRlZjE0MjhiODJhMmE4OTA3OTczN2Q3ZjVhZDA4MTQ5MTVlZmY1ZDdmNjgyNTk2OWYzIn19fQ");

        return npcCreator;
    }


    /**
     * 初始化设置矿场的建筑等级对照表
     */
    private void initStructure(){
        getBuildingLevelStructure().put(1,"junying1.structure");
        getBuildingLevelStructure().put(2,"junying2.structure");
    }

    @Override
    public int getMaxHealth() {
        return 300 + getLevel() * 50;
    }
}
