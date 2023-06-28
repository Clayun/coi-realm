package com.mcylm.coi.realm.cmd;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.runnable.MobSpawnPointTask;
import com.mcylm.coi.realm.tools.map.COIMobSpawnPoint;
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

public class SpawnPointCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player player) {
            if (player.hasPermission("coi.realm.spawnpoint")) {
                if (args.length >= 1 && args[0].equals("remove")) {
                    List<COIMobSpawnPoint> points = new ArrayList<>(Entry.getMapData().getMobSpawnPoints());
                    points.sort(Comparator.comparingDouble(vein -> vein.getLocation().getWorld() == player.getWorld() ? vein.getLocation().distance(player.getLocation()) : Integer.MAX_VALUE));
                    COIMobSpawnPoint point = points.get(0);
                    Entry.getMapData().getVeins().remove(point);
                    LoggerUtils.sendMessage("&c已删除离你最近的生成点: " + point.getLocation() ,player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Entry.getInstance().saveMapData();
                        }
                    }.runTaskAsynchronously(Entry.getInstance());
                } else if (args.length >= 1 && args[0].equals("create")) {

                    int maxRadius = Integer.parseInt(args[0]);
                    // String targetTeam = args[1];
                    Location location = player.getLocation();
                    COIMobSpawnPoint point = new COIMobSpawnPoint(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName(), maxRadius);
                    Entry.getMapData().getMobSpawnPoints().add(point);
                    LoggerUtils.sendMessage("&a成功添加", player);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Entry.getInstance().saveMapData();
                        }
                    }.runTaskAsynchronously(Entry.getInstance());
                } else if (args.length >= 1 && args[0].equalsIgnoreCase("test")) {
                    MobSpawnPointTask.runTask();

                }
            }
        }
        return false;
    }

}
