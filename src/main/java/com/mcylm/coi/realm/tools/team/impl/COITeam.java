package com.mcylm.coi.realm.tools.team.impl;

import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.tools.building.impl.COIBuilding;
import com.mcylm.coi.realm.tools.team.Team;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 小队
 */
@Data
public class COITeam implements Team {

    // 小队类型
    private COITeamType type;

    // 小队玩家列表（只记录名称）
    private List<String> players;

    // 队内NPC共享食物箱子位置
    private List<Location> foodChests;

    // 已经建造的建筑（允许有重复的）
    private List<COIBuilding> finishedBuildings;

    public COITeam(COITeamType type) {
        this.type = type;
        this.players = new ArrayList<>();
        this.foodChests = new ArrayList<>();
        this.finishedBuildings = new ArrayList<>();
    }

    /**
     * 加入小队
     * 如果玩家已经加入了其他的小队，会退出其他小队
     * @param player
     * @return
     */
    @Override
    public boolean join(Player player) {



        return false;
    }
}
