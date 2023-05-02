package com.mcylm.coi.realm.tools.selection;

import com.destroystokyo.paper.ParticleBuilder;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COIStructure;
import com.mcylm.coi.realm.tools.building.LineBuild;
import com.mcylm.coi.realm.utils.LocationUtils;
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

            List<Location> buildPoints = new ArrayList<>();
            for (Location point : line) {
                Block block = point.getWorld().getHighestBlockAt(point);
                buildPoints.add(point);
                Location loc = point.clone();
                for (int i = block.getY() ;i > 0 ;i--) {
                    Block block1 = loc.subtract(0,1,0).getBlock();
                    if (!building.pointCheck(block1)) {
                        canPlace = false;
                    }
                }
            }

            int i = 0;
            for (Location point : line) {
                i++;
                Block block = point.getWorld().getHighestBlockAt(point);

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

        if (canPlace) {

            new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {

                    List<Location> buildPoints = new ArrayList<>();
                    int extra = points.size() - (points.size() % building.getMaxLength());
                    for (Location point : points) {

                        i++;
                        buildPoints.add(point);
                        if ((i % building.getMaxLength() == 0 && i < extra)) {

                            LineBuild cloneBuilding = building.cloneBuild();
                            cloneBuilding.setPoints(buildPoints);

                            Entry.runSync(() -> cloneBuilding.build(null, player));
                            buildPoints = new ArrayList<>();

                            while (!cloneBuilding.isComplete()) { }
                        }



                    }

                    LineBuild cloneBuilding = building.cloneBuild();
                    cloneBuilding.setPoints(buildPoints);
                    Entry.runSync(() -> cloneBuilding.build(null, player));

                }
            }.runTaskAsynchronously(Entry.getInstance());
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
