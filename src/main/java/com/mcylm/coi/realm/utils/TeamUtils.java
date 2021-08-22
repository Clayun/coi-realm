package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGUIType;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 队伍控制工具类
 */
public class TeamUtils {

    /**
     * 初始化队伍
     */
    public static List<COITeam> initTeams(){
        // 默认初始化6个小队，等待倒计时结束会把有人的
        COITeam black = new COITeam(COITeamType.BLACK);
        COITeam red = new COITeam(COITeamType.RED);
        COITeam purple = new COITeam(COITeamType.PURPLE);
        COITeam green = new COITeam(COITeamType.GREEN);
        COITeam yellow = new COITeam(COITeamType.YELLOW);
        COITeam blue = new COITeam(COITeamType.BLUE);

        List<COITeam> results = new ArrayList<>();

        results.add(red);
        results.add(yellow);
        results.add(green);
        results.add(blue);
        results.add(purple);
        results.add(black);

        return results;
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

}
