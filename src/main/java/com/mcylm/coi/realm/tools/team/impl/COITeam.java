package com.mcylm.coi.realm.tools.team.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIScoreType;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.model.COIScore;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.team.Team;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 小队
 */
@Getter
@Setter
public class COITeam implements Team {

    // 小队类型
    // Team type
    private COITeamType type;

    // 小队玩家列表（只记录名称）
    // Players list (only name)
    private List<String> players;

    // 默认出生点
    private Location spawner;

    // 队内NPC共享食物箱子位置
    // Team npc share food chest location
    private List<Location> foodChests;

    // 大本营的共享资源箱子（存放各种东西）
    private List<Location> resourcesChests;

    // 已经建造的建筑（允许有重复的）
    // All built building(allow repeat buildings)
    private List<COIBuilding> finishedBuildings;

    // 小队共享的战斗队性
    // Team formation
    private List<List<Integer>> battleFormation;

    // 是否被击败（大本营被攻破）
    private boolean defeat = false;

    // 小队的积分
    // 用于结算小队玩家的奖励的权重
    private double score = 0;

    // 积分明细
    // TODO 本数据建议结算时存入数据库，可以用于计算成就等
    private List<COIScore> scoreRecords;

    public COITeam(COITeamType type,Location spawner) {
        this.type = type;
        this.players = new ArrayList<>();
        this.foodChests = new ArrayList<>();
        this.resourcesChests = new ArrayList<>();
        this.finishedBuildings = new ArrayList<>();
        this.battleFormation = new ArrayList<>();
        this.scoreRecords = new ArrayList<>();
        this.spawner = spawner;
        // 初始化大本营
        TeamUtils.initTeamBase(this);
    }

    /**
     * 加入小队
     * 如果玩家已经加入了其他的小队，会退出其他小队
     * Join a team
     * If player is already been join other's team,it will be quit.
     * @param player
     * @return
     */
    @Override
    public boolean join(Player player) {

        if(getPlayers().size() >= Entry.getInstance().getConfig().getInt("game.max-group-players")){
            // 加入失败，队伍满了
            // Join fail,the team is full.
            return false;
        }

        List<COITeam> teams = Entry.getGame().getTeams();

        Iterator<COITeam> iterator = teams.iterator();

        // 加入队伍之前，先退出其他队伍
        // You need quit your team before you join the new one.
        while(iterator.hasNext()){
            COITeam coiTeam = iterator.next();
            if(coiTeam.getPlayers().contains(player.getName())){
                coiTeam.getPlayers().remove(player.getName());
                break;
            }
        }

        // 加入队伍
        // Join team
        getPlayers().add(player.getName());

        return true;
    }

    /**
     * 查询指定类型的建筑
     * Search building type
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

    @Override
    public void manageScore(COIScoreType type, Player player) {

        if(type == null || player == null){
            return;
        }

        // 加减总积分
        score = score + type.getScore();

        COIScore coiScore = new COIScore(type, LocalDateTime.now(),player);

        // 存储记录
        getScoreRecords().add(coiScore);

    }

    /**
     * 失败
     * 1.记录双方小队的积分
     * 2.将所有玩家变换成观察者模式
     * @param player
     * @param team
     */
    @Override
    public void defeatedBy(Player player,COITeam team) {

        if(player == null && team == null){
            // 如果两个都是null，代表被野怪给干掉的
        }else{
            // 被玩家或者玩家的随从NPC干掉了
        }
    }

    /**
     * 获取GUI显示的内容
     * Get GUI view content for player name
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
