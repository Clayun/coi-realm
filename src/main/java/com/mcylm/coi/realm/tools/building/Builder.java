package com.mcylm.coi.realm.tools.building;

import com.mcylm.coi.realm.tools.building.COIPaster;
import org.bukkit.entity.Player;

public interface Builder {

    //自动建造建筑
    void pasteStructure(COIPaster paste);

    //玩家自动建造建筑，并发送消息
    void pasteStructure(COIPaster paste, Player player);

}
