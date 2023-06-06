package com.mcylm.coi.realm.tools.npc.monster;

import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import org.bukkit.entity.EntityType;

public abstract class COIMonsterCreator extends COINpc {
    public COIMonsterCreator() {
        super();
        setNpcType(EntityType.ZOMBIE);
    }

    public abstract void createMonster(COIBuilding building);
}
