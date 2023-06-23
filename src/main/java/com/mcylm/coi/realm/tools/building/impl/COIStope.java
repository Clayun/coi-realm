package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.tools.npc.COICartCreator;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.tools.npc.impl.COICart;
import com.mcylm.coi.realm.tools.npc.impl.COIMiner;
import com.mcylm.coi.realm.utils.GUIUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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
        // 默认等级为1
        setLevel(1);
        // 初始化NPC创建器
        setNpcCreators(initNPCCreator());
        //初始化完成，可建造
        setAvailable(true);
        initStructure();
    }

    @Override
    public BuildingConfig getDefaultConfig() {
        return new BuildingConfig()
                .setMaxLevel(3)
                .setConsume(64)
                .setMaxBuild(10)
                .setStructures(getBuildingLevelStructure());
    }

    @Override
    public void buildSuccess(Location location, Player player) {
        super.buildSuccess(location, player);

        for (COINpc creator : getNpcCreators()) {
            // 如果建筑建造完成，NPC就初始化
            if (isComplete()) {

                // 必须是符合建筑等级的要求的
                if(creator.getRequiredBuildingLevel() == getLevel()){
                    if(creator instanceof COIMinerCreator){
                        COIMinerCreator npcCreator = (COIMinerCreator) creator;
                        // 设置箱子
                        npcCreator.setChestsLocation(getChestsLocation());
                        COIMiner worker = new COIMiner(npcCreator);
                        worker.spawn(creator.getSpawnLocation());

                    }else if(creator instanceof COICartCreator){
                        COICartCreator npcCreator = (COICartCreator) creator;
                        npcCreator.setChestsLocation(getChestsLocation());
                        npcCreator.setToSaveResourcesLocations(getTeam().getResourcesChests());
                        COICart worker = new COICart(npcCreator);
                        worker.spawn(creator.getSpawnLocation());
                    }
                }
            }
        }



    }

    @Override
    public void upgradeBuildSuccess() {
        super.upgradeBuildSuccess();
        for (COINpc creator : getNpcCreators()) {

            // 判断是否符合NPC生成条件
            if(creator.getRequiredBuildingLevel() == getLevel()){
                if(creator instanceof COIMinerCreator){
                    COIMinerCreator npcCreator = (COIMinerCreator) creator;
                    // 设置箱子
                    npcCreator.setChestsLocation(getChestsLocation());
                    COIMiner worker = new COIMiner(npcCreator);
                    worker.spawn(creator.getSpawnLocation());

                }else if(creator instanceof COICartCreator){
                    COICartCreator npcCreator = (COICartCreator) creator;
                    npcCreator.setChestsLocation(getChestsLocation());
                    npcCreator.setToSaveResourcesLocations(getTeam().getResourcesChests());
                    COICart worker = new COICart(npcCreator);
                    worker.spawn(creator.getSpawnLocation());
                }
            }else{
                if(creator instanceof COIMinerCreator){
                    // 设置箱子
                    COIMinerCreator npcCreator = (COIMinerCreator) creator;
                    npcCreator.setChestsLocation(getChestsLocation());

                }else if(creator instanceof COICartCreator){
                    COICartCreator npcCreator = (COICartCreator) creator;
                    npcCreator.setChestsLocation(getChestsLocation());
                }
            }

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
        npcCreator.setNpcType(EntityType.PILLAGER);
        npcCreator.setDisguiseType(DisguiseType.PLAYER);
        npcCreator.setAggressive(false);
        npcCreator.setAlertRadius(5);
        npcCreator.setBreakBlockMaterials(breakBlockMaterials);
        npcCreator.setName("矿工");
        npcCreator.setLevel(1);
        npcCreator.setPickItemMaterials(pickItemMaterials);
        npcCreator.setFlagWatcherHandler(flagWatcher -> {
            PlayerWatcher playerWatcher = (PlayerWatcher) flagWatcher;
            playerWatcher.setSkin("TMSG4mes");
        });
        return npcCreator;
    }

    /**
     * 构造一个矿工NPC创建器
     *
     * @return
     */
    private COICartCreator initCartCreator() {

        // 背包内的物品
        Inventory inventory = GUIUtils.createNpcInventory(3);

        // 从配置文件读取矿工要捡起来的东西
//        List<String> picks = Entry.getInstance().getConfig().getStringList("miner.picks");
//        Set<String> pickItemMaterials = new HashSet<>();
//        pickItemMaterials.addAll(picks);


        COICartCreator npcCreator = new COICartCreator(getChestsLocation());
        npcCreator.setInventory(inventory);

        // 设置伪装
        npcCreator.setDisguiseType(DisguiseType.MINECART_CHEST);
        npcCreator.setNpcType(EntityType.PILLAGER);
        // 建筑2级之后再出生
        npcCreator.setRequiredBuildingLevel(2);

        npcCreator.setAggressive(false);
        npcCreator.setAlertRadius(5);
        npcCreator.setBreakBlockMaterials(new HashSet<>());
        npcCreator.setName("矿车");
        npcCreator.setLevel(1);
        npcCreator.setPickItemMaterials(new HashSet<>());

        return npcCreator;
    }
    
    private List<COINpc> initNPCCreator(){
        List<COINpc> npcList = new ArrayList<>();
        npcList.add(initCartCreator());
        npcList.add(initMinerCreator());
        
        return npcList;
    }

    /**
     * 初始化设置矿场的建筑等级对照表
     */
    private void initStructure() {
        getBuildingLevelStructure().put(1, "kuangchang1.structure");
        getBuildingLevelStructure().put(2, "kuangchang1.structure");
        getBuildingLevelStructure().put(3, "kuangchang1.structure");
    }

    @Override
    public int getMaxHealth() {
        return 100 + getLevel() * 50;
    }
}
