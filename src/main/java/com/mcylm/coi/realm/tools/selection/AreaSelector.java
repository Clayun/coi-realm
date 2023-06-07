package com.mcylm.coi.realm.tools.selection;

import com.destroystokyo.paper.ParticleBuilder;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.model.COIStructure;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.FloatableBuild;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.utils.particle.ParticleRect;
import com.mcylm.coi.realm.utils.region.Region;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Getter
@Setter
public class AreaSelector implements Selector {

    private Player player;
    private boolean canPlace = false;
    private Location selectedLocation;
    private COIBuilding building;
    private boolean stop;
    private float yaw;
    private COIStructure structure;
    private Set<FallingBlock> fakeBlocks = new HashSet<>();

    public AreaSelector(Player p, COIBuilding building, Location location) {
        this.player = p;
        this.selectedLocation = location;
        this.stop = false;
        this.building = building;
        selectors.put(p, this);

        String structureName = building.getStructureByLevel();

        if (structureName == null) {
            return;
        }
        this.yaw = player.getLocation().getYaw();

        location.setYaw(yaw);
        // 实例化建筑结构
        structure = building.prepareStructure(Entry.getBuilder().getStructureByFile(structureName), location.clone());
        createFakeBlocks();
        new BukkitRunnable() {
            @Override
            public void run() {

                if (stop) {
                    this.cancel();
                } else {
                    if (!player.isOnline()) {
                        stop(false);
                    }
                    select(selectedLocation, structure);
                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 5, 5); // 间隔修改为1/4秒
    }



    public void select(Location basicLocation, COIStructure structure) {

        List<COIBlock> allBlocks = structure.getBlocks();

        Vector maxXYZ = new Vector();

        Vector minXYZ = new Vector();
        boolean first = true;
        for (COIBlock coiBlock : allBlocks) {
            if (first) {
                maxXYZ.setX(coiBlock.getX());
                maxXYZ.setY(coiBlock.getY());
                maxXYZ.setZ(coiBlock.getZ());

                minXYZ = maxXYZ.clone();
                first = false;
            }

            if (maxXYZ.getX() < coiBlock.getX()) {
                maxXYZ.setX(coiBlock.getX());
            }
            if (maxXYZ.getY() < coiBlock.getY()) {
                maxXYZ.setY(coiBlock.getY());
            }
            if (maxXYZ.getZ() < coiBlock.getZ()) {
                maxXYZ.setZ(coiBlock.getZ());
            }


            if (minXYZ.getX() > coiBlock.getX()) {
                maxXYZ.setX(coiBlock.getX());
            }
            if (minXYZ.getY() > coiBlock.getY()) {
                minXYZ.setY(coiBlock.getY());
            }
            if (minXYZ.getZ() > coiBlock.getZ()) {
                maxXYZ.setZ(coiBlock.getZ());
            }
        }

        Location start = basicLocation.clone().add(minXYZ);
        Location end = basicLocation.clone().add(maxXYZ);

        Region region = new Region(start, end);

        canPlace = true;

        for (Block block : region.getBlocks()) {
            if (BuildData.getBuildingByBlock(block) != null) {
                canPlace = false;
            }

            Location clone = block.getLocation().clone();
            clone.setY(clone.getY() + 1);
            if(clone.getBlock().isSolid()){
                // 建筑只能在完全空白的地方建造
                canPlace = false;
            }
        }
        Region regionFloor = new Region(start.clone(), end.clone().set(end.getX(), start.getY() - 1, end.getZ()));
        Set<Block> blocks = regionFloor.getBlocks();
        int emptyCount = 0;
        for (Block block : blocks) {
            if (!block.isSolid()) {
                emptyCount++;
            }
        }
        if (!(building instanceof FloatableBuild)) {
            if ((float) emptyCount / blocks.size() >= 0.4) {
                canPlace = false;
            }
        }

        String state = canPlace ? "§a可放置" : "§c不可放置";

        player.sendActionBar("§a潜行进行放置 右键选择新点 §c切换物品取消 §e当前状态: " + state);


        ParticleRect rect = new ParticleRect(start, end.getZ() - start.getZ(), end.getX() - start.getX(), end.getY() - start.getY());

        if (player.isSneaking() && canPlace) {
            Bukkit.getScheduler().runTask(Entry.getInstance(), () -> place(structure));
        }

        rect.draw(canPlace ? new ParticleBuilder(Particle.REDSTONE).color(Color.LIME).receivers(player) : new ParticleBuilder(Particle.REDSTONE).color(Color.RED).receivers(player), player);
    }

    public void place(COIStructure structure) {
        List<COIBlock> allBlocks = structure.getBlocks();

        Vector maxXYZ = new Vector();

        Vector minXYZ = new Vector();
        boolean first = true;
        for (COIBlock coiBlock : allBlocks) {
            if (first) {
                maxXYZ.setX(coiBlock.getX());
                maxXYZ.setY(coiBlock.getY());
                maxXYZ.setZ(coiBlock.getZ());

                minXYZ = maxXYZ.clone();
                first = false;
            }

            if (maxXYZ.getX() < coiBlock.getX()) {
                maxXYZ.setX(coiBlock.getX());
            }
            if (maxXYZ.getY() < coiBlock.getY()) {
                maxXYZ.setY(coiBlock.getY());
            }
            if (maxXYZ.getZ() < coiBlock.getZ()) {
                maxXYZ.setZ(coiBlock.getZ());
            }


            if (minXYZ.getX() > coiBlock.getX()) {
                maxXYZ.setX(coiBlock.getX());
            }
            if (minXYZ.getY() > coiBlock.getY()) {
                minXYZ.setY(coiBlock.getY());
            }
            if (minXYZ.getZ() > coiBlock.getZ()) {
                maxXYZ.setZ(coiBlock.getZ());
            }
        }

        Location start = selectedLocation.clone().add(minXYZ);
        Location end = selectedLocation.clone().add(maxXYZ);

        Region region = new Region(start, end);

        canPlace = true;

        for (Block block : region.getBlocks()) {
            if (BuildData.getBuildingByBlock(block) != null) {
                canPlace = false;
            }
        }


        Region regionFloor = new Region(start.clone(), end.clone().set(end.getX(), start.getY() - 1, end.getZ()));
        Set<Block> blocks = regionFloor.getBlocks();
        int emptyCount = 0;
        for (Block block : blocks) {
            if (!block.isSolid()) {
                emptyCount++;
            }
        }
        if (!(building instanceof FloatableBuild)) {
            if ((float) emptyCount / blocks.size() >= 0.4) {
                canPlace = false;
            }
        }
        if (canPlace) {
            stop(false);
            selectedLocation.setYaw(yaw);
            building.build(selectedLocation, player);

        }
    }


    @Override
    public void selectLocation(Location loc) {
        selectedLocation = loc;

        this.yaw = player.getLocation().getYaw();

        selectedLocation.setYaw(yaw);
        // 实例化建筑结构
        structure = building.prepareStructure(Entry.getBuilder().getStructureByFile(building.getStructureByLevel()), selectedLocation.clone());

        createFakeBlocks();
    }

    private void createFakeBlocks() {

        fakeBlocks.forEach(Entity::remove);
        fakeBlocks.clear();

        // 这是个给BukkitRunnable的List
        List<FallingBlock> blocks = new ArrayList<>();

        List<COIBlock> coiBlocks = structure.getBlocks();
        int chunkSize = 64;
        List<List<COIBlock>> result = IntStream.range(0, (coiBlocks.size() + chunkSize - 1) / chunkSize)
                .mapToObj(i -> coiBlocks.subList(i * chunkSize, Math.min((i + 1) * chunkSize, coiBlocks.size())))
                .toList();

        if (structure != null) {
            int t = 0;
            for (List<COIBlock> part : result) {
                t++;
                new BukkitRunnable() {
                    @Override
                    public void run() {

                        for (COIBlock block : part) {
                            if (isStop()) {
                                this.cancel();
                                return;
                            }
                            if (Material.AIR.toString().equals(block.getMaterial())) {
                                continue;
                            }
                            Location blockLoc = selectedLocation.clone().add(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
                            if (blockLoc.getBlock().isSolid()) continue;
                            FallingBlock fallingBlock = selectedLocation.getWorld().spawnFallingBlock(blockLoc, Bukkit.createBlockData(block.getBlockData()));
                            fallingBlock.setSilent(true);
                            fallingBlock.setDropItem(false);
                            fallingBlock.setGravity(false);
                            fallingBlock.setMetadata("preview_block", new FixedMetadataValue(Entry.getInstance(), true));
                            // fallingBlock.setGlowing(true);

                            fallingBlock.setInvulnerable(true);
                            fakeBlocks.add(fallingBlock);
                            blocks.add(fallingBlock);

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p != player) {
                                    p.hideEntity(Entry.getInstance(), fallingBlock);
                                }
                            }
                        }
                    }
                }.runTaskLater(Entry.getInstance(), t);

            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                for (FallingBlock block : blocks) {
                    block.setTicksLived(10);
                    for (Player p: Bukkit.getOnlinePlayers()) {
                        if (p != player) {
                            p.hideEntity(Entry.getInstance(), block);
                        }
                    }
                    if (block.isDead() || stop) {
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(Entry.getInstance(), 0, 5);

    }

    @Override
    public void stop(boolean sendMsg) {
        setStop(true);
        if (sendMsg) player.sendActionBar("§c已取消");

        selectors.remove(player);
        Entry.runSync(() -> {

            fakeBlocks.forEach(e -> {
                e.getChunk().load();
                e.remove();
            });
            fakeBlocks.clear();
        });
    }

}
