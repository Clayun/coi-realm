package com.mcylm.coi.realm.tools.npc;

import com.mcylm.coi.realm.model.COINpc;
import org.bukkit.entity.EntityType;

public class COIMonsterCreator extends COINpc {
    public COIMonsterCreator() {
        super();
        setNpcType(EntityType.ZOMBIE);
    }
}
