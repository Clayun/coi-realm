package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.tools.npc.impl.COIMiner;
import com.mcylm.coi.realm.utils.GUIUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 采矿场
 * 用于收集矿物资源的
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class COIStope extends COIBuilding {

    public COIStope() {
        // 设置建筑类型为矿场
        // 默认等级为1
        setLevel(1);
        // 最高等级为2级
        setMaxLevel(2);
        // 设置等级对应的建筑文件
        initStructure();
        // 初始化NPC创建器
        setNpcCreators(List.of(initMinerCreator()));
        // 矿场设置所需消耗的材料
        setConsume(64);
        //初始化完成，可建造
        setAvailable(true);
    }

    @Override
    public void buildSuccess(Location location, Player player) {


        for (COINpc creator : getNpcCreators()) {
            // 如果建筑建造完成，NPC就初始化
            if (isComplete()) {
                COIMinerCreator npcCreator = (COIMinerCreator) creator;
                // 设置箱子
                npcCreator.setChestsLocation(getChestsLocation());
                COIMiner worker = new COIMiner(npcCreator);
                worker.spawn(creator.getSpawnLocation());


            }
        }



    }

    @Override
    public void upgradeBuildSuccess() {
        super.upgradeBuildSuccess();
        for (COINpc creator : getNpcCreators()) {

            COIMinerCreator npcCreator = (COIMinerCreator) creator;
            npcCreator.setChestsLocation(getChestsLocation());
        }
    }

    /**
     * 构造一个矿工NPC创建器
     *
     * @return
     */
    private COIMinerCreator initMinerCreator() {

        // 背包内的物品
        Inventory inventory = GUIUtils.createNpcInventory(3);

        // 从配置文件读取矿工要挖掘的方块名称
        List<String> breaks = Entry.getInstance().getConfig().getStringList("miner.breaks");
        Set<String> breakBlockMaterials = new HashSet<>();
        breakBlockMaterials.addAll(breaks);

        // 从配置文件读取矿工要挖掘的方块名称
        List<String> picks = Entry.getInstance().getConfig().getStringList("miner.picks");
        Set<String> pickItemMaterials = new HashSet<>();
        pickItemMaterials.addAll(picks);

        // 衣服默认捡起
        List<Material> clothes = COINpc.CLOTHES;

        for (Material clothesType : clothes) {
            pickItemMaterials.add(clothesType.name());
        }

        COIMinerCreator npcCreator = new COIMinerCreator(getChestsLocation());
        npcCreator.setInventory(inventory);
        inventory.addItem(new ItemStack(Material.IRON_PICKAXE));

        npcCreator.setAggressive(false);
        npcCreator.setAlertRadius(5);
        npcCreator.setBreakBlockMaterials(breakBlockMaterials);
        npcCreator.setName("矿工");
        npcCreator.setLevel(1);
        npcCreator.setPickItemMaterials(pickItemMaterials);
        npcCreator.setSkinName("Miner");
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
    private void initStructure() {
        getBuildingLevelStructure().put(1, "kuangchang1.structure");
        getBuildingLevelStructure().put(2, "kuangchang2.structure");
    }

    @Override
    public int getMaxHealth() {
        return 100 + getLevel() * 50;
    }
}
