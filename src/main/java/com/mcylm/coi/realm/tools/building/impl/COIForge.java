package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.tools.npc.COISmithCreator;
import com.mcylm.coi.realm.tools.npc.impl.COIFarmer;
import com.mcylm.coi.realm.tools.npc.impl.COISmith;
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
 * 铁匠铺
 */
public class COIForge extends COIBuilding {

    public COIForge() {
        // 默认等级为1
        setLevel(1);
        // 初始化NPC创建器
        setNpcCreators(List.of(initSmithCreator()));
        //初始化完成，可建造
        setAvailable(true);
        initStructure();
    }

    @Override
    public BuildingConfig getDefaultConfig() {

        return new BuildingConfig()
                .setStructures(getBuildingLevelStructure())
                .setMaxLevel(5)
                .setMaxBuild(1)
                .setConsume(1024);
    }

    @Override
    public void buildSuccess(Location location, Player player) {
        super.buildSuccess(location, player);

        // 如果建筑建造完成，NPC就初始化
        for (COINpc creator : getNpcCreators()) {
            if (isComplete()) {
                COISmithCreator npcCreator = (COISmithCreator) creator;
                // 设置食物收集箱子
                COISmith smith = new COISmith(npcCreator);
                smith.spawn(creator.getSpawnLocation());
            }
        }
    }




    @Override
    public void upgradeBuildSuccess() {
        super.upgradeBuildSuccess();

        // 给小队成员，NPC全部新套装
    }


    /**
     * 构造一个NPC创建器
     *
     * @return
     */
    private COINpc initSmithCreator() {

        // 背包内的物品
        Inventory inventory = GUIUtils.createNpcInventory(3);


        COISmithCreator npcCreator = new COISmithCreator();
        npcCreator.setInventory(inventory);
        npcCreator.setNpcType(EntityType.PILLAGER);
        npcCreator.setDisguiseType(DisguiseType.PLAYER);
        npcCreator.setAggressive(false);
        npcCreator.setAlertRadius(5);
        npcCreator.setBreakBlockMaterials(new HashSet<>());
        npcCreator.setName("铁匠");
        npcCreator.setLevel(1);
        npcCreator.setPickItemMaterials(new HashSet<>());

        npcCreator.setFlagWatcherHandler(flagWatcher -> {
            PlayerWatcher playerWatcher = (PlayerWatcher) flagWatcher;
            playerWatcher.setSkin("KowalLosu");
        });

        return npcCreator;
    }

    private void initStructure() {
        getBuildingLevelStructure().put(1, "tiejiangpu1.structure");
        getBuildingLevelStructure().put(2, "tiejiangpu1.structure");
        getBuildingLevelStructure().put(3, "tiejiangpu1.structure");
        getBuildingLevelStructure().put(4, "tiejiangpu1.structure");
        getBuildingLevelStructure().put(5, "tiejiangpu1.structure");
    }

    @Override
    public int getMaxHealth() {
        return 1000 + getLevel() * 100;
    }
}
