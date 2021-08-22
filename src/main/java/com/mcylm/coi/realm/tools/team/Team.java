package com.mcylm.coi.realm.tools.team;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.tools.building.impl.COIBuilding;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 小队抽象层
 */
public interface Team {

    // 创建全部小队
    boolean join(Player player);

    // 通过建筑类型获取当前小队所建造的建筑
    List<COIBuilding> getBuildingByType(COIBuildingType type);

}
