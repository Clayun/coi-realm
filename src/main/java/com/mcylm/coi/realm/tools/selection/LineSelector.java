package com.mcylm.coi.realm.tools.selection;

import com.destroystokyo.paper.ParticleBuilder;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COIStructure;
import com.mcylm.coi.realm.tools.building.LineBuild;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Setter
@Getter
public class LineSelector implements Selector {

    private Player player;
    private boolean canPlace = false;
    private Location start;
    private Location end;

    private LineBuild building;
    private boolean stop;


    public LineSelector(Player p, LineBuild building, Location location) {

        this.player = p;
        this.building = building;
        this.start = location;
        location.add(0.5,0,0.5);

        selectors.put(p, this);
        String structureName = building.getStructureByLevel();

        if (structureName == null) {
            return;
        }

        // 实例化建筑结构
        COIStructure structure = building.prepareStructure(Entry.getBuilder().getStructureByFile(structureName), location.clone());

        new BukkitRunnable() {
            @Override
            public void run() {

                if (stop) {
                    this.cancel();
                } else {
                    if (!player.isOnline()) {
                        stop(false);
                    }
                    select(structure);
                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 8, 8);
    }

    public void select(COIStructure structure) {

        canPlace = true;
        new ParticleBuilder(Particle.REDSTONE).color(Color.YELLOW).location(start.clone().add(0,2,0)).receivers(player).spawn();
        if (end != null) {
            new ParticleBuilder(Particle.REDSTONE).color(Color.YELLOW).location(end.clone().add(0,2,0)).receivers(player).spawn();

            List<Location> line = LocationUtils.line(start, end, 1);
            if (line.size() < 2) {
                return;
            }
            int length = building.getMaxLength();

            List<Block> blocks = new ArrayList<>();
            Iterator<Location> iterator = line.iterator();
            while (iterator.hasNext()) {
                Location point = iterator.next();
                Block block = point.getWorld().getHighestBlockAt(point);
                if (blocks.contains(block)) {
                    iterator.remove();
                    continue;
                }
                blocks.add(block);
                for (int i = 0 ; i < block.getY() ;i++) {
                    Block block1 = point.getWorld().getBlockAt(point.getBlockX(), i, point.getBlockZ());
                    if (!building.pointCheck(block1)) {
                        canPlace = false;
                    }
                }
            }

            List<Location> buildPoints = new ArrayList<>();

            int i = 0;
            for (Location point : line) {
                i++;
                Block block = point.getWorld().getHighestBlockAt(point);

                buildPoints.add(block.getLocation());
                ParticleBuilder builder = new ParticleBuilder(Particle.REDSTONE);
                builder.color(canPlace ? Color.LIME : Color.RED);
                if (i % length == 0) {
                    builder.color(Color.BLACK);
                }
                @NotNull Location particleLoc = block.getLocation().add(0.5, 1.5, 0.5);
                builder.location(particleLoc).receivers(player).spawn();
            }

            String state = canPlace ? "§a可放置" : "§c不可放置";

            player.sendActionBar("§a潜行进行放置 右键选择新点 §c切换物品取消 §e当前状态: " + state);


            if (player.isSneaking() && canPlace) {
                stop(false);
                Bukkit.getScheduler().runTask(Entry.getInstance(), () -> place(buildPoints));
            }
        } else {
            player.sendActionBar("§a右键选择点 §c切换物品取消 §e当前状态: §f需要选点");

        }
    }


    public void place(List<Location> points) {

        for (Location point : points) {
            Block block = point.getWorld().getHighestBlockAt(point);

            Location loc = point.clone();
            for (int i = block.getY() ;i > 0 ;i--) {
                Block block1 = loc.subtract(0,1,0).getBlock();
                if (!building.pointCheck(block1)) {
                    canPlace = false;
                }
            }
        }

        List<Location> buildPoints = new ArrayList<>();
        AtomicInteger wallCount = new AtomicInteger(0);
        int maxLength = building.getMaxLength();

        CompletableFuture.allOf(IntStream.range(0, points.size())
                .boxed()
                .collect(Collectors.groupingBy(index -> index / maxLength))
                .values()
                .stream()
                .map(subList -> CompletableFuture.runAsync(() -> {
                    subList.forEach(index -> {
                        Location point = points.get(index);
                        buildPoints.add(point);
                        int wallIndex = wallCount.incrementAndGet();
                        if (wallIndex % maxLength == 0) {
                            LoggerUtils.debug("wall:" + wallIndex);
                            LineBuild cloneBuilding = building.cloneBuild();
                            cloneBuilding.setPoints(List.copyOf(buildPoints));
                            cloneBuilding.build(null, player);
                            buildPoints.clear();
                        }
                    });
                })).toArray(CompletableFuture[]::new)).join();

        if (!buildPoints.isEmpty()) {
            LineBuild cloneBuilding = building.cloneBuild();
            cloneBuilding.setPoints(buildPoints);
            cloneBuilding.build(null, player);
        }

    }



    @Override
    public boolean isStop() {
        return stop;
    }

    @Override
    public void stop(boolean sendMsg) {
        setStop(true);
        if (sendMsg) player.sendActionBar("§c已取消");

        selectors.remove(player);
    }

    @Override
    public void selectLocation(Location loc) {

        loc.add(0.5,0,0.5);

        end = loc;

    }
}
