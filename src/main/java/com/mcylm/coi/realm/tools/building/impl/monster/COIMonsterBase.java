package com.mcylm.coi.realm.tools.building.impl.monster;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.tools.npc.monster.COIMonsterCreator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class COIMonsterBase extends COIBuilding {

    public COIMonsterBase() {
        setLevel(1);
        setAvailable(true);
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
        new BukkitRunnable() {

            @Override
            public void run() {

                // 如果建筑建造完成，NPC就初始化
                if(isComplete()){
                    for (COINpc creator : getNpcCreators()) {
                        COIMonsterCreator npcCreator = (COIMonsterCreator) creator;

                        npcCreator.createMonster(building);
                    }
                }
            }
        }.runTaskTimer(Entry.getInstance(),0, 20L);


    }
    @Override
    public int getMaxHealth() {
        return 500;
    }
}
