package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.tools.npc.impl.COIFarmer;
import com.mcylm.coi.realm.utils.GUIUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 磨坊
 */
public class COIMill extends COIBuilding {

    public COIMill() {
        // 默认等级为1
        setLevel(1);
        // 初始化NPC创建器
        setNpcCreators(List.of(initFarmerCreator()));
        //初始化完成，可建造
        setAvailable(true);
        initStructure();
    }

    @Override
    public BuildingConfig getDefaultConfig() {

        return new BuildingConfig()
                .setStructures(getBuildingLevelStructure())
                .setMaxLevel(2)
                .setConsume(100);
    }

    @Override
    public void buildSuccess(Location location, Player player) {
        super.buildSuccess(location, player);

        // 如果建筑建造完成，NPC就初始化
        for (COINpc creator : getNpcCreators()) {

            if (isComplete()) {
                COIMinerCreator npcCreator = (COIMinerCreator) creator;
                // 设置食物收集箱子
                npcCreator.setChestsLocation(getChestsLocation());
                COIFarmer farmer = new COIFarmer(npcCreator);
                farmer.spawn(creator.getSpawnLocation());
                // 为小队的其他NPC共享食物箱子
                TeamUtils.getTeamByPlayer(player).getFoodChests().addAll(getChestsLocation());

            }
        }
    }




    @Override
    public void upgradeBuildSuccess() {
        super.upgradeBuildSuccess();
        for (COINpc creator : getNpcCreators()) {
            COIMinerCreator npcCreator = (COIMinerCreator) creator;
            // 设置食物收集箱子
            npcCreator.setChestsLocation(getChestsLocation());
            getTeam().getFoodChests().addAll(getChestsLocation());
        }
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
        Inventory inventory = GUIUtils.createNpcInventory(3);
        inventory.addItem(new ItemStack(Material.IRON_HOE));

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
        npcCreator.setNpcType(EntityType.PILLAGER);
        npcCreator.setDisguiseType(DisguiseType.PLAYER);
        npcCreator.setAggressive(false);
        npcCreator.setAlertRadius(5);
        npcCreator.setBreakBlockMaterials(breakBlockMaterials);
        npcCreator.setName("农民");
        npcCreator.setLevel(1);
        npcCreator.setPickItemMaterials(pickItemMaterials);

        npcCreator.setFlagWatcherHandler(flagWatcher -> {
            PlayerWatcher playerWatcher = (PlayerWatcher) flagWatcher;
            playerWatcher.setSkin("farmer");
        });

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
