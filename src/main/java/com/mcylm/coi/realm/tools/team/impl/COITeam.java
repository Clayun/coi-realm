package com.mcylm.coi.realm.tools.team.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.tools.building.impl.COIBuilding;
import com.mcylm.coi.realm.tools.team.Team;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
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

        if(getPlayers().size() >= 5){
            // 加入失败，队伍满了
            return false;
        }

        List<COITeam> teams = Entry.getGame().getTeams();

        Iterator<COITeam> iterator = teams.iterator();

        // 加入队伍之前，先退出其他队伍
        while(iterator.hasNext()){
            COITeam coiTeam = iterator.next();
            if(coiTeam.getPlayers().contains(player.getName())){
                coiTeam.getPlayers().remove(player.getName());
                break;
            }
        }

        // 加入队伍
        getPlayers().add(player.getName());

        return true;
    }

    /**
     * 查询指定类型的建筑
     * @param type
     * @return
     */
    @Override
    public List<COIBuilding> getBuildingByType(COIBuildingType type) {

        List<COIBuilding> finishedBuildings = getFinishedBuildings();

        List<COIBuilding> results = new ArrayList<>();

        if(finishedBuildings == null
            || finishedBuildings.isEmpty()){
            return new ArrayList<>();
        }

        for(COIBuilding coiBuilding : finishedBuildings){
            if(coiBuilding.getType().equals(type)){
                results.add(coiBuilding);
            }
        }

        return results;
    }

    /**
     * 获取GUI显示的内容
     * @return
     */
    public List<String> getPlayerListName(){

        List<String> format = new ArrayList<>();

        for(String player : getPlayers()){
            format.add("        &6"+player);
        }

        return format;
    }
}
