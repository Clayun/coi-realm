package com.mcylm.coi.realm.tools.building.impl.monster;

import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.tools.npc.monster.COIPillagerCreator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class COIMonsterBase extends COIBuilding {

    public COIMonsterBase() {
        setLevel(1);
        //初始化完成，可建造
        setAvailable(true);
        // 初始化NPC创建器
        setNpcCreators(List.of(
                // 一个野怪小队3个人
                COIPillagerCreator.initCOIPillagerCreator(null),
                COIPillagerCreator.initCOIPillagerCreator(null),
                COIPillagerCreator.initCOIPillagerCreator(null)
                ));
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
    public void buildSuccess(Location location, Player player) {

        if (isComplete()) {
            for (COINpc creator : getNpcCreators()) {
                COIPillagerCreator npcCreator = (COIPillagerCreator) creator;

                npcCreator.createMonster(this);
            }
        }
    }

    @Override
    public int getMaxHealth() {
        return 500;
    }

    private void initStructure(){
        getBuildingLevelStructure().put(1,"monster1.structure");
        getBuildingLevelStructure().put(2,"monster1.structure");
        getBuildingLevelStructure().put(3,"monster1.structure");
    }
}
