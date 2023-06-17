package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.tools.npc.COICartCreator;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.tools.npc.impl.COICart;
import com.mcylm.coi.realm.tools.npc.impl.COIMiner;
import com.mcylm.coi.realm.tools.npc.impl.COISoldier;
import com.mcylm.coi.realm.utils.GUIUtils;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 军营
 * 可以生成战士
 */
public class COICamp extends COIBuilding {

    public COICamp() {
        initStructure();
        // 默认等级为1
        setLevel(1);
        // 初始化NPC创建器
        setNpcCreators(List.of(initSoldierCreator(1), initSoldierCreator(2), initSoldierCreator(3)));
        //初始化完成，可建造
        setAvailable(true);
    }

    @Override
    public BuildingConfig getDefaultConfig() {

        return new BuildingConfig()
                .setMaxLevel(3)
                .setMaxBuild(5)
                .setConsume(512)
                .setStructures(getBuildingLevelStructure());
    }

    @Override
    public void buildSuccess(Location location, Player player) {
        super.buildSuccess(location, player);

        for (COINpc creator : getNpcCreators()) {
            // 必须是符合建筑等级的要求的
            if(creator.getRequiredBuildingLevel() == getLevel()){
                COISoldierCreator npcCreator = (COISoldierCreator) creator;

                // 设置NPC跟随创造建筑的玩家
                npcCreator.setFollowPlayerName(player.getName());
                // 初始化
                COISoldier soldier = new COISoldier(npcCreator);
                soldier.spawn(creator.getSpawnLocation());
                // 仅用于跟随的 Commander
                soldier.setCommander(player);
            }
        }

    }

    @Override
    public void upgradeBuildSuccess() {
        super.upgradeBuildSuccess();
        for (COINpc creator : getNpcCreators()) {

            // 判断是否符合NPC生成条件
            if(creator.getRequiredBuildingLevel() == getLevel()){
                COISoldierCreator npcCreator = (COISoldierCreator) creator;

                // 设置NPC跟随创造建筑的玩家
                npcCreator.setFollowPlayerName(getBuildPlayerName());
                // 初始化
                COISoldier soldier = new COISoldier(npcCreator);
                soldier.spawn(creator.getSpawnLocation());
                // 仅用于跟随的 Commander

                Player player = Bukkit.getPlayer(getBuildPlayerName());
                soldier.setCommander(player);
            }

        }
    }

    /**
     * 构造一个战士NPC创建器
     * @return
     */
    private COISoldierCreator initSoldierCreator(int level){

        // 背包内的物品
        Inventory inventory = GUIUtils.createNpcInventory(3);
//        inventory.addItem(new ItemStack(new Random().nextBoolean() ? Material.CROSSBOW : Material.IRON_SWORD));
        inventory.addItem(new ItemStack(Material.LEATHER_HELMET));
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

        npcCreator.setRequiredBuildingLevel(level);
        npcCreator.setNpcType(EntityType.PILLAGER);

        npcCreator.setDisguiseType(DisguiseType.PLAYER);
        npcCreator.setInventory(inventory);
        npcCreator.setAggressive(true);
        npcCreator.setAlertRadius(10);
        npcCreator.setBreakBlockMaterials(breakBlockMaterials);
        npcCreator.setName("战士");
        npcCreator.setLevel(1);
        npcCreator.setPickItemMaterials(pickItemMaterials);

        npcCreator.setFlagWatcherHandler(flagWatcher -> {
            PlayerWatcher playerWatcher = (PlayerWatcher) flagWatcher;
            playerWatcher.setSkin("greatapedude");
        });


        return npcCreator;
    }


    /**
     * 初始化设置矿场的建筑等级对照表
     */
    private void initStructure(){
        getBuildingLevelStructure().put(1,"junying1.structure");
        getBuildingLevelStructure().put(2,"junying1.structure");
        getBuildingLevelStructure().put(3,"junying1.structure");
    }

    @Override
    public int getMaxHealth() {
        return 300 + getLevel() * 50;
    }
}
