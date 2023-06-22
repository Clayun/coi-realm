package com.mcylm.coi.realm.tools.selection;

import com.destroystokyo.paper.ParticleBuilder;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.model.COIStructure;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.FloatableBuild;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.particle.ParticleRect;
import com.mcylm.coi.realm.utils.region.Region;
import lombok.Data;
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

@Data
public class FloatableSelector implements Selector{

    private Player player;
    private boolean canPlace = false;
    private Location selectedLocation;
    private COIBuilding building;
    private boolean stop;
    private float yaw;
    private COIStructure structure;
    private Set<FallingBlock> fakeBlocks = new HashSet<>();

    public FloatableSelector(Player p, COIBuilding building, Location location) {
        this.player = p;

        this.selectedLocation = location;
        // 检测 selectedLocation 周围是空气的位置并选中
        if(!checkAirLocation()){
            LoggerUtils.sendMessage("&c当前建筑不能建造在这里",p);
            return;
        }else{
            location = getAirLocation(location);
            this.selectedLocation = location;
        }

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

    private boolean checkAirLocation(){

        if(getAirLocation(this.selectedLocation) != null){
            return true;
        }

        return false;
    }

    private Location getAirLocation(Location location){
        // 判断前后左右
        // 即x+1/-1 z+1/-1这四个位置，如果都不是空气，之间false
        Location clone = location.clone();
        clone.setX(clone.getX() + 1);
        if(!clone.getBlock().isSolid()){
            return clone;
        }

        Location clone2 = location.clone();
        clone2.setX(clone2.getX() - 1);
        if(!clone2.getBlock().isSolid()){
            return clone2;
        }

        Location clone3 = location.clone();
        clone3.setZ(clone3.getZ() + 1);
        if(!clone3.getBlock().isSolid()){
            return clone3;
        }

        Location clone4 = location.clone();
        clone4.setZ(clone4.getZ() - 1);
        if(!clone4.getBlock().isSolid()){
            return clone4;
        }

        return null;
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
            COIBuilding buildingByBlock = BuildData.getBuildingByBlock(block);
            if (!(buildingByBlock instanceof FloatableBuild)) {
                canPlace = false;
            }

            // 这个时候要判断是否脚底下是虚空，必须是虚空才能造
            double height = block.getLocation().getY();

            for(int i = Entry.BRIDGE_DETECT_HEIGHT ;i < height;i++){
                Block clone3 = block.getWorld().getBlockAt(block.getX(), i, block.getZ());
                if(!clone3.getType().equals(Material.AIR)){
                    canPlace = false;
                    break;
                }
            }
        }

        if(!checkAirLocation()){
            canPlace = false;
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

            COIBuilding buildingByBlock = BuildData.getBuildingByBlock(block);
            if (buildingByBlock != null
                && !buildingByBlock.getType().equals(COIBuildingType.BRIDGE)) {
                canPlace = false;
            }

            // 这个时候要判断是否脚底下是虚空，必须是虚空才能造
            double height = block.getLocation().getY();

            for(int i = Entry.BRIDGE_DETECT_HEIGHT ;i < height;i++){
                Block clone3 = block.getWorld().getBlockAt(block.getX(), i, block.getZ());
                if(!clone3.getType().equals(Material.AIR)){
                    canPlace = false;
                    break;
                }
            }
        }

        if(!checkAirLocation()){
            canPlace = false;
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
                            // fallingBlock.setGlowing(true); // 可能会让客户端很卡?

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
                }.runTaskLater(Entry.getInstance(), t); // 延迟生成 防止Geyser Bug

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
