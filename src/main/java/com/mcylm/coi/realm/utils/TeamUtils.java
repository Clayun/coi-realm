package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.EntityData;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 队伍控制工具类
 */
public class TeamUtils {

    @Getter
    private static COITeam monsterTeam;
    /**
     * 初始化队伍
     */
    public static List<COITeam> initTeams(){

        int configTeams = Entry.getInstance().getConfig().getInt("game.max-teams");

        if(configTeams < 2 || configTeams > 6){
            LoggerUtils.log("&4请注意！团队数量必须在2~6个之间！！！当前团队数量有误");
        }

        // 团队数量
        int maxTeam = configTeams;

        List<String> locations = Entry.getInstance().getConfig().getStringList("game.spawn-locations");
        String world = Entry.getInstance().getConfig().getString("game.spawn-world");

        List<Location> spawnerList = new ArrayList<>();

        // 如果小于队伍数量，就让后面几个队伍重复
        if(locations.size() < maxTeam){

            LoggerUtils.log("&4请注意！团队出生点数量少于团队数量，无法正常进行游戏！！！");

            for(String str : locations){
                Location location = getLocation(str, world);
                spawnerList.add(location);
            }

            // 有几个队伍是没有出生点的
            int num = maxTeam - locations.size();

            for(int i = 0; i <= num; i++){
                Location location = getLocation(locations.get(0), world);
                spawnerList.add(location);
            }

        }else{
            // 如果有多个出生点，大于队伍数量，就随机取点，不能重复
            List<Integer> usedCursor = new ArrayList<>();

            while (usedCursor.size() < maxTeam){
                Random rand = new Random();
                int value = rand.nextInt(maxTeam);
                if(usedCursor.contains(value)){
                    continue;
                }

                usedCursor.add(value);
                spawnerList.add(getLocation(locations.get(value), world));
            }


        }

        // 生成好的队伍
        List<COITeam> results = new ArrayList<>();

        COITeamType[] values = COITeamType.values();

        int count = 0;

        for(COITeamType type : values){

            if(count < maxTeam){
                results.add(new COITeam(type,spawnerList.get(count)));
            }

            count ++;
        }

        // 获取怪物小队的出生点
        // 初始化一个怪物小队
        String spawner = Entry.getInstance().getConfig().getString("game.monster-spawner");

        Location location = getLocation(spawner, world);
        monsterTeam = new COITeam(COITeamType.MONSTER, location);
        results.add(monsterTeam);

        return results;
    }

    private static Location getLocation(String locStr,String world){
        String[] split = locStr.split(",");
        return new Location(Entry.getInstance().getServer().getWorld(world),
                Double.parseDouble(split[0]),Double.parseDouble(split[1]),Double.parseDouble(split[2]));
    }

    /**
     * 判断两个玩家是否在同一个队伍
     * @param player1
     * @param player2
     * @return
     */
    public static boolean inSameTeam(String player1,String player2){
        COITeam team = getTeamByPlayer(player1);

        if(team == null){
            return false;
        }

        if(team.getPlayers().contains(player2)){
            return true;
        }

        return false;
    }

    /**
     * 判断玩家是否在当前队伍当中
     * @param player
     * @param team
     * @return
     */
    public static boolean inTeam(String player,COITeam team){

        if(team.getPlayers().contains(player)){
            return true;
        }

        return false;
    }

    /**
     * 获取玩家所在小队
     * @return
     */
    public static COITeam getTeamByPlayer(Player player){

        List<COITeam> teams = Entry.getGame().getTeams();

        Iterator<COITeam> iterator = teams.iterator();

        // 查询所有队伍
        while(iterator.hasNext()){
            COITeam coiTeam = iterator.next();
            if(coiTeam.getPlayers().contains(player.getName())){
                return coiTeam;
            }
        }

        return null;
    }

    public static COITeam getTeamByPlayer(String player){

        List<COITeam> teams = Entry.getGame().getTeams();

        Iterator<COITeam> iterator = teams.iterator();

        // 查询所有队伍
        while(iterator.hasNext()){
            COITeam coiTeam = iterator.next();
            if(coiTeam.getPlayers().contains(player)){
                return coiTeam;
            }
        }

        return null;
    }

    /**
     * 获取敌对小队列表
     * @param team
     * @return
     */
    public static List<COITeam> getEnemyTeams(COITeam team){

        List<COITeam> result = new ArrayList<>();

        List<COITeam> teams = Entry.getGame().getTeams();

        Iterator<COITeam> iterator = teams.iterator();

        // 查询所有队伍
        while(iterator.hasNext()){
            COITeam coiTeam = iterator.next();
            if(coiTeam != team && !coiTeam.getType().equals(COITeamType.MONSTER)){
                result.add(coiTeam);
            }
        }

        return result;
    }

    /**
     * 获取一个人数最少的队伍
     * @return
     */
    public static COITeam getMinPlayersTeam(){
        List<COITeam> teams = Entry.getGame().getTeams();

        Iterator<COITeam> iterator = teams.iterator();

        COITeam minimumTeam = null;
        int players = 999;

        // 查询所有队伍
        while(iterator.hasNext()){
            COITeam coiTeam = iterator.next();

            // 修改为仅小于，这样就不会反复进入最后一个队伍
            if(coiTeam.getPlayers().size() < players
                && coiTeam.getType().getSlot() != -1){
                minimumTeam = coiTeam;
                players = coiTeam.getPlayers().size();
            }
        }

        if(players >= Entry.getInstance().getConfig().getInt("game.max-group-players")){
            return null;
        }

        return minimumTeam;
    }

    /**
     * 传送玩家到队伍默认的出生点
     * 本方法仅适用于游戏开始后使用
     * @param p
     */
    public static void tpSpawner(Player p){

        COITeam teamByPlayer = getTeamByPlayer(p);
        if(teamByPlayer == null){
            p.kick(Component.text("没队伍自动踢出服务器"));
            return;
        }
        Location spawner = teamByPlayer.getSpawner();
        p.teleport(spawner);

    }

    /**
     * 初始化小队的基地
     * 并返回玩家的出生点
     * @param team
     */
    public static void initTeamBase(COITeam team){
        Location spawner = team.getSpawner();

        try {
            COIBuilding building = Entry.getInstance().getBuildingManager().getBuildingTemplateByType(COIBuildingType.BASE);
            building.setTeam(team);
            building.build(spawner,team,true);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 全部玩家都传送到各自的出生点
     */
    public static void tpAllPlayersToSpawner(){

        // 同步线程去TP
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){
                    tpSpawner(p);
                }
            }
        }.runTaskLater(Entry.getInstance(), 0);

    }

    /**
     * 自动将玩家匹配一个队伍
     */
    public static void autoJoinTeam(){

        for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){

            COITeam team = TeamUtils.getTeamByPlayer(p);

            if(team == null){
                // 玩家没选择队伍
                // 自动选择一个人数最少的队伍丢进去
                COITeam minPlayersTeam = TeamUtils.getMinPlayersTeam();

                if(minPlayersTeam == null){
                    // 全都满了，直接给当前玩家踢了吧
                    p.kick(Component.text("当前服务器已满，请更换服务器后重试"), PlayerKickEvent.Cause.KICK_COMMAND);
                    return;
                }

                // 替玩家加入小队
                minPlayersTeam.join(p);

                Title title = Title.title(
                        Component.text(LoggerUtils.replaceColor("&a您被匹配至 "+minPlayersTeam.getType().getColor()+minPlayersTeam.getType().getName()+"")),
                        Component.text(LoggerUtils.replaceColor("&f使用背包里的 &c建筑蓝图 &f开始游戏吧")),
                        Title.DEFAULT_TIMES);
                p.showTitle(title);

            }
        }

    }

    public static void autoJoinTeam(Player p){

        COITeam team = TeamUtils.getTeamByPlayer(p);

        if(team == null){
            // 玩家没选择队伍
            // 自动选择一个人数最少的队伍丢进去
            COITeam minPlayersTeam = TeamUtils.getMinPlayersTeam();

            if(minPlayersTeam == null){
                // 全都满了，直接给当前玩家踢了吧
                p.kick(Component.text("当前服务器已满，请更换服务器后重试"), PlayerKickEvent.Cause.KICK_COMMAND);
                return;
            }

            // 替玩家加入小队
            minPlayersTeam.join(p);

            Title title = Title.title(
                    Component.text(LoggerUtils.replaceColor("&a您被匹配至 "+minPlayersTeam.getType().getColor()+minPlayersTeam.getType().getName()+"")),
                    Component.text(LoggerUtils.replaceColor("&f使用背包里的 &c建筑蓝图 &f开始游戏吧")),
                    Title.DEFAULT_TIMES);
            p.showTitle(title);

        }

    }

    /**
     * 判断NPC是否是所在小队的
     * @param entity
     * @param team
     * @return
     */
    public static boolean checkNPCInTeam(Entity entity, COITeam team){
        @Nullable COINpc data = EntityData.getNpcByEntity(entity);

        if(data == null){
            return false;
        }

        if (data.getTeam() != team) {
            return false;
        }

        return true;
    }

    /**
     * 获取NPC所在的队伍
     * @param entity
     * @return
     */
    public static COITeam getNPCTeam(Entity entity){
        @Nullable COINpc data = EntityData.getNpcByEntity(entity);

        if (data != null && data.getTeam() != null) {
            return data.getTeam();

        }

        return null;
    }

    /**
     * 获取NPC的所属人
     * @param entity
     * @return
     */
    public static String getNPCOwner(Entity entity){
        @Nullable COINpc data = EntityData.getNpcByEntity(entity);

        if (data != null && data.getBuilding() != null) {
            return data.getBuilding().getBuildPlayerName();

        }

        return null;
    }

}
