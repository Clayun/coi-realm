package com.mcylm.coi.realm.tools.building.impl.monster;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.tools.npc.monster.COIMonsterCreator;
import com.mcylm.coi.realm.tools.npc.monster.COIPillagerCreator;
import com.mcylm.coi.realm.utils.GUIUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class COIMonsterBase extends COIBuilding {

    public COIMonsterBase() {
        setLevel(1);
        //初始化完成，可建造
        setAvailable(true);
        initStructure();
    }

    @Setter
    @Getter
    private boolean canBeDamaged;
    @Override
    public BuildingConfig getDefaultConfig() {
        return new BuildingConfig()
                .setMaxLevel(3)
                .setConsume(-1)
                .setShowInMenu(false)
                .setStructures(getBuildingLevelStructure());
    }

    @Override
    public void damage(Entity attacker, int damage, Block attackBlock) {
        if (canBeDamaged) {
            super.damage(attacker, damage, attackBlock);
        }
    }

    @Override
    public void build(Location location, Player player){
        super.build(location,player);

        COIBuilding building = this;
        // 如果建筑建造完成，NPC就初始化
        if(isComplete()){
            for (COINpc creator : getNpcCreators()) {
                COIPillagerCreator npcCreator = (COIPillagerCreator) creator;

                npcCreator.createMonster(building);
            }
        }

    }


    @Override
    public int getMaxHealth() {
        return 500;
    }

    /**
     * 初始化设置矿场的建筑等级对照表
     */
    private void initStructure(){
        getBuildingLevelStructure().put(1,"monster1.structure");
    }
}
