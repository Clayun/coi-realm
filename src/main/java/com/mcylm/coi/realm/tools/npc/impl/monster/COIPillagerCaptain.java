package com.mcylm.coi.realm.tools.npc.impl.monster;


import com.mcylm.coi.realm.tools.npc.monster.COIPillagerCreator;

// 小队队长 属于精英怪
public class COIPillagerCaptain extends COIPillager {
    public COIPillagerCaptain(COIPillagerCreator npcCreator) {
        super(npcCreator);
    }

    @Override
    public int getDamage() {
        return 10;
    }


}
