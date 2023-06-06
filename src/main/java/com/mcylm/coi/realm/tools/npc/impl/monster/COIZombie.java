package com.mcylm.coi.realm.tools.npc.impl.monster;

import com.mcylm.coi.realm.tools.npc.COIMonsterCreator;

public class COIZombie extends COIMonster {


    public COIZombie(COIMonsterCreator npcCreator) {
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
