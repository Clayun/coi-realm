package com.mcylm.coi.realm.tools.team;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIScoreType;
import com.mcylm.coi.realm.model.COIScore;
import com.mcylm.coi.realm.tools.building.COIBuilding;
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

    // 给当前小队增加或者减少积分
    void addScore(COIScoreType type,Player player);

    // 被另一个队伍击败
    void defeatedBy(Player player,COITeam team);

}
