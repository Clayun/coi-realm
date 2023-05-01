package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.tools.npc.impl.COIFarmer;
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
 * 磨坊
 */
public class COIMill extends COIBuilding {

    public COIMill() {
        // 设置建筑类型为磨坊
        setType(COIBuildingType.MILL);
        // 默认等级为1
        setLevel(1);
        // 最高等级为2级
        setMaxLevel(2);
        // 设置等级对应的建筑文件
        initStructure();
        // 初始化NPC创建器
        setNpcCreator(initFarmerCreator());
        // 磨坊设置所需消耗的材料
        setConsume(32);
        //初始化完成，可建造
        setAvailable(true);
    }

    @Override
    public void buildSuccess(Location location, Player player) {
        // 如果需要创建NPC，就启动线程
        if (getNpcCreator() != null) {


            // 如果建筑建造完成，NPC就初始化
            if (isComplete()) {
                COIMinerCreator npcCreator = (COIMinerCreator) getNpcCreator();
                // 设置食物收集箱子
                npcCreator.setChestsLocation(getChestsLocation());
                COIFarmer farmer = new COIFarmer(npcCreator);
                farmer.spawn(getNpcCreator().getSpawnLocation());
                // 为小队的其他NPC共享食物箱子
                TeamUtils.getTeamByPlayer(player).getFoodChests().addAll(getChestsLocation());

            }
        }
    }




    @Override
    public void upgradeBuildSuccess() {
        super.upgradeBuildSuccess();
        COIMinerCreator npcCreator = (COIMinerCreator) getNpcCreator();
        // 设置食物收集箱子
        npcCreator.setChestsLocation(getChestsLocation());
        getTeam().getFoodChests().addAll(getChestsLocation());

    }

    @Override
    public void upgradeBuild(Player player) {
        getTeam().getFoodChests().removeAll(getChestsLocation());

        super.upgradeBuild(player);
    }

    /**
     * 构造一个农民NPC创建器
     *
     * @return
     */
    private COIMinerCreator initFarmerCreator() {

        // 背包内的物品
        List<ItemStack> inventory = new ArrayList<>();
        // 石锄
        ItemStack pickaxe = new ItemStack(Material.STONE_HOE);
        inventory.add(pickaxe);

        // 收割小麦
        Set<String> breakBlockMaterials = new HashSet<>();
        breakBlockMaterials.add("WHEAT");

        // 捡起的东西
        Set<String> pickItemMaterials = new HashSet<>();
        pickItemMaterials.add("APPLE");
        pickItemMaterials.add("BREAD");
        pickItemMaterials.add("WHEAT");

        COIMinerCreator npcCreator = new COIMinerCreator(getChestsLocation());
        npcCreator.setInventory(inventory);
        npcCreator.setAggressive(false);
        npcCreator.setAlertRadius(5);
        npcCreator.setBreakBlockMaterials(breakBlockMaterials);
        npcCreator.setName("农民");
        npcCreator.setLevel(1);
        npcCreator.setPickItemMaterials(pickItemMaterials);
        npcCreator.setSkinName("Farmer");
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

    private void initStructure() {
        getBuildingLevelStructure().put(1, "mofang1.structure");
        getBuildingLevelStructure().put(2, "mofang2.structure");
    }

    @Override
    public int getMaxHealth() {
        return 100 + getLevel() * 50;
    }
}
