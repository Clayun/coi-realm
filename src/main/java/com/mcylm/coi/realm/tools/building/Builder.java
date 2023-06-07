package com.mcylm.coi.realm.tools.building;

import com.mcylm.coi.realm.model.COIPaster;
import org.bukkit.entity.Player;

public interface Builder {

    //自动建造建筑
    void pasteStructure(COIPaster paste, COIBuilding building);

    //玩家自动建造建筑，并发送消息
    void pasteStructure(COIPaster paste, Player player, COIBuilding building);

    // 建造一个不归属COIBuilding的结构
    void pasteStructureWithoutBuilding(COIPaster paste, Player player);
}
