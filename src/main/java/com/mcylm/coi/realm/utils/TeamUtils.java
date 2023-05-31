package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGUIType;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 队伍控制工具类
 */
public class TeamUtils {

    /**
     * 初始化队伍
     */
    public static List<COITeam> initTeams(){

        List<String> locations = Entry.getInstance().getConfig().getStringList("game.spawn-locations");
        String world = Entry.getInstance().getConfig().getString("game.spawn-world");

        List<Location> spawnerList = new ArrayList<>();

        // 如果小于队伍数量，就让后面几个队伍重复
        if(locations.size() < 6){
            for(String str : locations){
                Location location = getLocation(str, world);
                spawnerList.add(location);
            }

            // 有几个队伍是没有出生点的
            int num = 6 - locations.size();

            for(int i = 0; i <= num; i++){
                Location location = getLocation(locations.get(0), world);
                spawnerList.add(location);
            }

        }else if(locations.size() >= 6){
            // 如果有多个出生点，大于队伍数量，就随机取点，不能重复
            List<Integer> usedCursor = new ArrayList<>();

            while (usedCursor.size() < locations.size()){
                Random rand = new Random();
                int value = rand.nextInt(6);
                if(usedCursor.contains(value)){
                    continue;
                }

                usedCursor.add(value);
                spawnerList.add(getLocation(locations.get(value), world));
            }


        }

        // 默认初始化6个小队，等待倒计时结束会把所有人传送到默认出生点
        COITeam black = new COITeam(COITeamType.BLACK,spawnerList.get(0));
        COITeam red = new COITeam(COITeamType.RED,spawnerList.get(1));
        COITeam purple = new COITeam(COITeamType.PURPLE,spawnerList.get(2));
        COITeam green = new COITeam(COITeamType.GREEN,spawnerList.get(3));
        COITeam yellow = new COITeam(COITeamType.YELLOW,spawnerList.get(4));
        COITeam blue = new COITeam(COITeamType.BLUE,spawnerList.get(5));

        List<COITeam> results = new ArrayList<>();

        results.add(red);
        results.add(yellow);
        results.add(green);
        results.add(blue);
        results.add(purple);
        results.add(black);

        return results;
    }

    private static Location getLocation(String locStr,String world){
        String[] split = locStr.split(",");
        return new Location(Entry.getInstance().getServer().getWorld(world),
                Double.parseDouble(split[0]),Double.parseDouble(split[1]),Double.parseDouble(split[2]));
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
            if(coiTeam.getPlayers().size() <= players){
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

        if(Entry.getGame().getStatus().equals(COIGameStatus.GAMING)){
            COITeam teamByPlayer = getTeamByPlayer(p);
            if(teamByPlayer == null){
                p.kick(Component.text("没队伍自动踢出服务器"));
                return;
            }
            Location spawner = teamByPlayer.getSpawner();
            p.teleport(spawner);
        }

    }

    /**
     * 全部玩家都传送到各自的出生点
     */
    public static void tpAllPlayersToSpawner(){
        for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){
            tpSpawner(p);
        }
    }

}
