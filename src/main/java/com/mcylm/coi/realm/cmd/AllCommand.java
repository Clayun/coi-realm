package com.mcylm.coi.realm.cmd;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.runnable.VeinGenerateTask;
import com.mcylm.coi.realm.tools.map.COIVein;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AllCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player player) {
            if (args.length >= 1) {
                if(Entry.getGame().getStatus().equals(COIGameStatus.GAMING)){

                    // 消息
                    String message = String.join(" ", args);

                    COITeam teamByPlayer = TeamUtils.getTeamByPlayer(player);
                    // 游戏中
                    for(Player p : Bukkit.getOnlinePlayers()){
                        LoggerUtils.sendAllChatMessage(teamByPlayer.getType().getColor()+"<"+player.getName()+">" + " &f"+message, p);
                    }

                    // 记录聊天日志
                    LoggerUtils.log(teamByPlayer.getType().getColor()+"<"+player.getName()+">" + " &f"+message);

                    return true;
                }else{
                    LoggerUtils.sendAllChatMessage("&7游戏尚未开始，无需全局喊话",player);

                    return true;
                }
            }
        }
        return false;
    }
}
