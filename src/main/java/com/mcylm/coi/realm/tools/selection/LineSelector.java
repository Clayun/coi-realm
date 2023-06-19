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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        location.add(0.5, 0, 0.5);

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
                    try {
                        Thread.sleep(50 * 8);
                        run();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }

            }
        }.runTaskAsynchronously(Entry.getInstance());
    }

    public void select(COIStructure structure) {

        canPlace = true;
        new ParticleBuilder(Particle.REDSTONE).color(Color.YELLOW).location(start.clone().add(0, 2, 0)).receivers(player).spawn();
        if (end != null) {
            new ParticleBuilder(Particle.REDSTONE).color(Color.YELLOW).location(end.clone().add(0, 2, 0)).receivers(player).spawn();

            List<Location> line = LocationUtils.line(start, end, 1);
            if (line.size() < 2) {
                return;
            }
            int length = building.getMaxLength();

            List<Block> blocks = new ArrayList<>();
            Iterator<Location> iterator = line.iterator();
//            LocalDateTime startTime = LocalDateTime.now();
            while (iterator.hasNext()) {
                Location point = iterator.next();
                Block block = getSuitableBlock(structure, point);
                if (blocks.contains(block)) {
                    iterator.remove();
                    continue;
                }
                blocks.add(block);
                for (int i = block.getY(); i < 256; i++) {
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
                Block block = getSuitableBlock(structure, point);
                buildPoints.add(block.getLocation());
                ParticleBuilder builder = new ParticleBuilder(Particle.REDSTONE);
                builder.color(canPlace ? Color.LIME : Color.RED);
                if (i % length == 0) {
                    builder.color(Color.BLACK);
                }
                @NotNull Location particleLoc = block.getLocation().add(0.5, 1.5, 0.5);
                builder.location(particleLoc).receivers(player).spawn();
            }

//            LocalDateTime endTime = LocalDateTime.now();
//            Duration duration = Duration.between(startTime, endTime);
//            long millis = duration.toMillis();
//            LoggerUtils.debug("select方法执行所需时间（毫秒）："+millis);

            String state = canPlace ? "§a可放置" : "§c不可放置";

            player.sendActionBar("§a潜行进行放置 右键选择新点 §c切换物品取消 §e当前状态: " + state);

            if (player.isSneaking() && canPlace) {
                stop(false);
                LoggerUtils.debug("玩家蹲下确认的建造");
                Bukkit.getScheduler().runTask(Entry.getInstance(), () -> place(buildPoints));
            }
        } else {
            player.sendActionBar("§a右键选择点 §c切换物品取消 §e当前状态: §f需要选点");

        }
    }

    private Block getSuitableBlock(COIStructure structure, Location loc) {
        loc = loc.clone();
        loc.setY(Entry.WALL_DETECT_HEIGHT);
        int freeHeight = 0;
        Block rootBlock = loc.getBlock();
        while (loc.getY() < 256) {
            if (freeHeight >= structure.getHeight()) {
                return rootBlock;
            }
            if (!loc.getBlock().isSolid()) {
                freeHeight++;
            } else {
                freeHeight = 0;
                rootBlock = loc.getBlock();
            }
            loc.add(0,1,0);

        }
        return rootBlock;
    }


    public void place(List<Location> points) {

        for (Location point : points) {
            Block block = point.getBlock();
            Location loc = point.clone();
            for (int i = block.getY(); i < 256; i++) {
                Block block1 = loc.add(0, 1, 0).getBlock();
                if (!building.pointCheck(block1)) {
                    canPlace = false;
                }
            }
        }

        if (canPlace) {



            Iterator<Location> iterator = points.listIterator();

            List<Location> buildPoints = new ArrayList<>();
            new BukkitRunnable() {

                int i = 0;

                LineBuild lastCloneBuilding;
                int extra = points.size() - (points.size() % building.getMaxLength());

                @Override
                public void run() {

                    if (!iterator.hasNext()) {
                        this.cancel();
                        return;
                    }

                    if (lastCloneBuilding == null || lastCloneBuilding.isComplete()) {
                        Location point = iterator.next();

                        i++;

                        buildPoints.add(point.clone());
                        if ((i % building.getMaxLength() == 0) || (i == points.size())) {

                            lastCloneBuilding = building.cloneBuild();
                            lastCloneBuilding.setPoints(List.copyOf(buildPoints));

                            Entry.runSync(() -> lastCloneBuilding.build(null, player));
                            buildPoints.clear();
                        }
                    }
                }
            }.runTaskTimerAsynchronously(Entry.getInstance(), 0, 1);

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

        loc.add(0.5, 0, 0.5);

        end = loc;

    }
}
