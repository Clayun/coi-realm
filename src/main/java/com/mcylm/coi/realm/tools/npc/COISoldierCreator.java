package com.mcylm.coi.realm.tools.npc;

import lombok.Data;
import org.bukkit.entity.Player;

import java.util.List;

@Data
public class COISoldierCreator extends COINpc{

    // NPC所在阵型编号
    private int npcNumber;

    // 阵型
    private List<List<Integer>> formats;

    public COISoldierCreator(int npcNumber, List<List<Integer>> formats) {
        this.npcNumber = npcNumber;
        this.formats = formats;
    }
}
