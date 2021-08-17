package com.mcylm.coi.realm.tools.npc;

import lombok.Data;
import org.bukkit.entity.Player;

@Data
public class COISoldierCreator extends COINpc{

    private int npcNumber;

    public COISoldierCreator(int npcNumber) {
        this.npcNumber = npcNumber;
    }
}
