package com.mcylm.coi.realm.cmd;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.map.COIVein;
import com.mcylm.coi.realm.utils.LoggerUtils;
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

public class VeinCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player player) {
            if (player.hasPermission("coi.realm.vein")) {
                if (args.length >= 1 && args[0].equals("remove")) {
                    List<COIVein> veins = new ArrayList<>(Entry.getMapData().getVeins());
                    veins.sort(Comparator.comparingDouble(vein -> vein.getLocation().getWorld() == player.getWorld() ? vein.getLocation().distance(player.getLocation()) : Integer.MAX_VALUE));
                    COIVein vein = veins.get(0);
                    Entry.getMapData().getVeins().remove(vein);
                    LoggerUtils.sendMessage("&c已删除离你最近的矿脉: " + vein.getLocation() ,player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Entry.getInstance().saveMapData();
                        }
                    }.runTaskAsynchronously(Entry.getInstance());
                } else if (args.length >= 1 && args[0].equals("create")) {
                    // create <structure> <chance> <resetTime>

                    String structureName = args[1];
                    double chance = (args.length >= 3) ? Double.parseDouble(args[2]) : 0.5;
                    int resetTime = (args.length >= 4) ? Integer.parseInt(args[3]) : 60;
                    Location location = player.getLocation();
                    COIVein vein = new COIVein(structureName, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getWorld().getName(), chance, resetTime);
                    Entry.getMapData().getVeins().add(vein);
                    LoggerUtils.sendMessage("&a成功添加", player);

                }
            }
        }
        return false;
    }
}
