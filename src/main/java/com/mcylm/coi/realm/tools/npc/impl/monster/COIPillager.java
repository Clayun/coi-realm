package com.mcylm.coi.realm.tools.npc.impl.monster;

import com.mcylm.coi.realm.tools.npc.monster.COIPillagerCreator;

public class COIPillager extends COIMonster {


    public COIPillager(COIPillagerCreator npcCreator) {
        super(npcCreator);
    }

    @Override
    public int getDamage() {
        return 5;
    }

    @Override
    public int delayTick() {
        return 5;
    }
}
