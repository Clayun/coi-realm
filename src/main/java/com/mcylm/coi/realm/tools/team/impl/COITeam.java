package com.mcylm.coi.realm.tools.team.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIScoreType;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.model.COIScore;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.team.Team;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

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

    // 失败后的记录
    private List<String> playersCache;

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
        // 初始化本小队的计分板
        registerScoreboardTeam();
        this.players = new ArrayList<>();
        this.playersCache = new ArrayList<>();
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
     * 注册小队
     * @return
     */
    private void registerScoreboardTeam() {
        org.bukkit.scoreboard.Team team = Entry.getInstance().getScoreboard().getTeam(getType().getCode());
        if (team == null) {
            Entry.getInstance().getScoreboard().registerNewTeam(getType().getCode());
        }
    }

    /**
     * 将玩家添加入计分板
     * @param name
     */
    private void addPlayerToScoreboard(String name){
        org.bukkit.scoreboard.Team team = Entry.getInstance().getScoreboard().getTeam(getType().getCode());
        team.addEntry(name);
    }

    /**
     * 添加实体到本小队
     * @param entity
     */
    public void addEntityToScoreboard(Entity entity){
        org.bukkit.scoreboard.Team team = Entry.getInstance().getScoreboard().getTeam(getType().getCode());
        team.addEntity(entity);
    }

    /**
     * 从计分板里删除生物
     * @param entity
     */
    public void removeEntityFromScoreboard(Entity entity){
        if(entity == null){
            return;
        }
        org.bukkit.scoreboard.Team team = Entry.getInstance().getScoreboard().getTeam(getType().getCode());
        team.removeEntity(entity);
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

        // 计分板添加玩家
        addPlayerToScoreboard(player.getName());

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
    public void addScore(COIScoreType type, Player player) {

        if(type == null || player == null){
            return;
        }

        // 失败后不允许加积分
        if(isDefeat()){
            return;
        }

        // 加减总积分
        score = score + type.getScore();

        COIScore coiScore = new COIScore(type, LocalDateTime.now(),player);

        // 存储记录
        getScoreRecords().add(coiScore);

        // 提示信息
        LoggerUtils.sendActionbar(coiScore.toString(),player);

    }

    /**
     * 通过Entity给所属人添加积分
     * @param type
     * @param entity
     */
    public void addScore(COIScoreType type, Entity entity){
        String npcOwner = TeamUtils.getNPCOwner(entity);

        if(npcOwner != null){
            Player player = Bukkit.getPlayer(npcOwner);

            if(player != null){
                addScore(type,player);
            }
        }else{
            if(entity instanceof Player player){
                // 判断entity是否玩家

                if(TeamUtils.getTeamByPlayer(player) != null){
                    addScore(type,player);
                }
            }
        }


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

        // 设为失败
        setDefeat(true);

        if(player == null || team == null){
            // 不存在
        }else{
            // 击败小队奖励埋点
            team.addScore(COIScoreType.BEAT_TEAM,player);
        }

        // 被玩家或者玩家的随从NPC干掉了
        // 整队在怪物队伍复活
        // 如果怪物队伍被拆了，则直接失败

        // 全员转移到怪物队伍
        setPlayersCache(getPlayers());
        TeamUtils.getMonsterTeam().getPlayers().addAll(getPlayers());
        setPlayers(new ArrayList<>());

        // 清理背包并传送到新的出生点
        for(String playerName : getPlayersCache()){
            Player defeatPlayer = Bukkit.getPlayer(playerName);
            if(defeatPlayer != null){
                defeatPlayer.getInventory().clear();
                if(defeatPlayer.isOnline()){
                    defeatPlayer.teleport(TeamUtils.getMonsterTeam().getSpawner());
                }
            }
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

    public List<String> getEnemyPlayers(){
        List<String> players = new ArrayList<>();
        for(COITeam team : Entry.getGame().getTeams()){
            if(team.equals(this)){
                continue;
            }
            players.addAll(team.getPlayers());

        }

        return players;
    }

    /**
     * 获取团队绿宝石总数
     * @return
     */
    public int getTotalEmerald(){
        int count = 0;

        String material = Entry.getInstance().getConfig().getString("game.building.material");

        // 先计算箱子里存了多少个
        for(COIBuilding building : getFinishedBuildings()){
            if(!building.getChestsLocation().isEmpty()){
                // 总存储箱子
                for(Location loc : building.getChestsLocation()){

                    int num = ItemUtils.getItemAmountFromContainer(loc, Material.getMaterial(material));
                    count = count + num;
                }
            }
        }

        // 再获取玩家身上的
        for (String name : getPlayers()) {
            Player player = Bukkit.getPlayer(name);

            if(player.isOnline()){
                int num = ItemUtils.getItemAmountFromInventory(player.getInventory(), Material.getMaterial(material));

                count = count + num;
            }

        }

        return count;
    }

    /**
     * 获取公共绿宝石资源总数
     * @return
     */
    public int getPublicEmerald(){
        int count = 0;

        String material = Entry.getInstance().getConfig().getString("game.building.material");

        // 先计算箱子里存了多少个
        for(COIBuilding building : getFinishedBuildings()){

            if(building.getType().equals(COIBuildingType.BASE)){
                // 仅限于大本营的箱子才算做公共绿宝石资源
                if(!building.getChestsLocation().isEmpty()){
                    // 总存储箱子
                    for(Location loc : building.getChestsLocation()){

                        int num = ItemUtils.getItemAmountFromContainer(loc, Material.getMaterial(material));
                        count = count + num;
                    }
                }

                return count;
            }

        }

        return count;
    }

    /**
     * 获取团队总人口
     * @return
     */
    public int getTotalPeople(){
        int total = getPlayers().size();

        // 计算总NPC数量
        for(COIBuilding building : getFinishedBuildings()){
            int size = building.getNpcCreators().size();

            total = total + size;
        }

        return total;
    }

    public COIBuilding getBase(){
        for(COIBuilding building : getFinishedBuildings()){
            if(building.getType().equals(COIBuildingType.BASE)){
                return building;
            }
        }

        return null;
    }


}
