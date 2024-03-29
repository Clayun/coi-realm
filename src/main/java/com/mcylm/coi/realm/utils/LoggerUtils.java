package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIServerMode;
import me.lucko.helper.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class LoggerUtils {

    public static void log(String msg){
        Entry.getInstance().getLogger().info("["+Entry.PREFIX+" INFO] "+replaceColor(msg));
    }

    public static void debug(String msg){
        //只有开发模式才输出的日志
        if(COIServerMode.DEVELOP.getCode().equals(Entry.SERVER_MODE)){
            Entry.getInstance().getLogger().info("["+Entry.PREFIX+" DEBUG] "+replaceColor(msg));
        }

    }

    public static void sendMessage(String msg, CommandSender player){

        if(player == null){
            return;
        }

        player.sendMessage("§f[§b§l"+Entry.PREFIX+"§f] §7"+replaceColor(msg));
    }

    public static void sendMessage(String msg, Player player){

        if(player == null){
            return;
        }

        if(player.isOnline()){
            player.sendMessage("§f[§b§l"+Entry.PREFIX+"§f] §7"+replaceColor(msg));
        }
    }

    public static void sendAllChatMessage(String msg, Player player){

        if(player == null){
            return;
        }

        if(player.isOnline()){
            player.sendMessage("§f[§e全局 §c/all§f] §7"+replaceColor(msg));
        }
    }

    public static void sendTeamChatMessage(String msg, Player player){

        if(player == null){
            return;
        }

        if(player.isOnline()){
            player.sendMessage("§f[§b队内§f] §7"+replaceColor(msg));
        }
    }

    public static String replaceColor(String msg){
        return msg.replace("&","§");
    }

    public static void sendActionbar(Player player, String message) {

        if(player != null && StringUtils.isNotBlank(player.getName())) {
            player = Bukkit.getPlayer(player.getName());
        }

        if (player == null || message == null || !player.isOnline()) return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(replaceColor(message)));

    }

    public static void sendActionbar( String message,Player player) {
        sendActionbar(player,message);
    }

    public static void broadcastMessage(String message){

        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.isOnline()){
                player.sendMessage("§f[§a公告§f] §7"+replaceColor(message));
            }
        }
    }



}
