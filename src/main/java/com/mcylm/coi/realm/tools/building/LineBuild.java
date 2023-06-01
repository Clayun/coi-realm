package com.mcylm.coi.realm.tools.building;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.model.COIPaster;
import com.mcylm.coi.realm.model.COIStructure;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@Getter
@Setter
public abstract class LineBuild extends COIBuilding {

    private List<Location> points = new ArrayList<>();


    public int getMaxLength() {
        return 8;
    }

    public void build(Location ignored, Player player) {

        if (!isAvailable()) {
            return;
        }

        // 扣除玩家背包里的资源
        boolean b = deductionResources(player);

        if (!b) {
            LoggerUtils.sendMessage("背包里的资源不够，请去收集资源", player);
            return;
        }

        // 建筑开始就记录位置
        Location location = points.get(points.size() / 2).clone();
        setLocation(location.clone());
        setWorld(location.getWorld().getName());

        String structureName = getStructureByLevel();

        if (structureName == null) {
            return;
        }
        // 实例化建筑结构
        COIStructure structure = Entry.getBuilder().getStructureByFile(structureName);


        // 设置名称
        structure.setName(getType().getName());

        structure = prepareStructure(structure, location.clone());

        // 预先计算建筑的方块位置，及总方块数量
        List<COIBlock> allBlocks = getAllBlocksByStructure(structure);
        setRemainingBlocks(allBlocks);
        setTotalBlocks(allBlocks.size() * points.size());

        // 设置NPC所属小队
        getNpcCreators().forEach(npcCreator -> {
            npcCreator.setTeam(TeamUtils.getTeamByPlayer(player));
            npcCreator.setBuilding(this);
        });

        COIBuilding building = this;

        processLine(points);

        Iterator<Location> iterator = points.iterator();
        LoggerUtils.debug(String.valueOf(points));
        // 开始建造

        Set<COIPaster> pasters = new HashSet<>();

        COIStructure finalStructure = structure;
        BukkitTask task = new BukkitRunnable() {

            @Override
            public void run() {


                Location point = iterator.next();

                LoggerUtils.debug("build");
                // 构造一个建造器
                COIPaster coiPaster = new COIPaster(false, getType().getUnit(), getType().getInterval()
                        , location.getWorld().getName(), point,null
                        , finalStructure.clone(), false, TeamUtils.getTeamByPlayer(player).getType().getBlockColor()
                        , getNpcCreators(), ((block, blockToPlace, type) -> {
                    getBlocks().add(block);
                    //block.setMetadata("building", new BuildData(building));
                    if (ItemUtils.SUITABLE_CONTAINER_TYPES.contains(type)) {
                        getChestsLocation().add(block.getLocation());
                    }
                    getOriginalBlockData().putIfAbsent(block.getLocation(), block.getBlockData().clone());
                    getOriginalBlocks().putIfAbsent(block.getLocation(), block.getType());
                    return type;
                }));

                pasters.add(coiPaster);

                Entry.getBuilder().pasteStructure(coiPaster, building);

                getTeam().getFinishedBuildings().add(building);
                if (!iterator.hasNext()) {
                    this.cancel();
                }

            }
        }.runTaskTimer(Entry.getInstance(), 1, 2);
        new BukkitRunnable() {
            @Override
            public void run() {

                if (task.isCancelled()) {
                    setComplete(pasters.stream().allMatch(COIPaster::isComplete));

                    if (isComplete()) {

                        LoggerUtils.debug("complete");
                        Bukkit.getScheduler().runTask(Entry.getInstance(), () -> {
                            buildSuccess(location, player);
                        });
                        this.cancel();
                    }
                }


            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 0L, 1L);
    }


    @Override
    public void upgradeBuild(Player player) {


        for (Block b : getBlocks()) {
            b.removeMetadata("building", Entry.getInstance());

        }
        Set<Map.Entry<Location, Material>> blocks = getOriginalBlocks().entrySet();
        Set<Map.Entry<Location, BlockData>> blockData = getOriginalBlockData().entrySet();
        for (Map.Entry<Location, Material> entry : blocks) {
            Block block = entry.getKey().getBlock();
            if (block.getState() instanceof Container container) {
                for (ItemStack item : container.getInventory().getContents()) {
                    if (item != null) block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
            block.setType(entry.getValue());
        }
        for (Map.Entry<Location, BlockData> entry : blockData) {
            entry.getKey().getBlock().setBlockData(entry.getValue());
        }

        getBlocks().clear();
        getOriginalBlocks().clear();
        getOriginalBlockData().clear();
        getRemainingBlocks().clear();
        getChestsLocation().clear();


        // 建筑开始就记录位置
        Location location = points.get(points.size() / 2).clone();
        setLocation(location.clone());
        setWorld(location.getWorld().getName());

        String structureName = getStructureByLevel();

        if (structureName == null) {
            return;
        }
        // 实例化建筑结构
        COIStructure structure = Entry.getBuilder().getStructureByFile(structureName);


        // 设置名称
        structure.setName(getType().getName());

        structure = prepareStructure(structure, location.clone());

        // 预先计算建筑的方块位置，及总方块数量
        List<COIBlock> allBlocks = getAllBlocksByStructure(structure);
        setRemainingBlocks(allBlocks);
        setTotalBlocks(allBlocks.size());

        COIBuilding building = this;

        processLine(points);

        Iterator<Location> iterator = points.iterator();
        LoggerUtils.debug(String.valueOf(points));
        // 开始建造

        Set<COIPaster> pasters = new HashSet<>();

        COIStructure finalStructure = structure;
        BukkitTask task = new BukkitRunnable() {

            @Override
            public void run() {


                Location point = iterator.next();

                LoggerUtils.debug("build");
                // 构造一个建造器
                COIPaster coiPaster = new COIPaster(false, getType().getUnit(), getType().getInterval()
                        , location.getWorld().getName(), point,null
                        , finalStructure.clone(), false, TeamUtils.getTeamByPlayer(player).getType().getBlockColor()
                        , getNpcCreators(), ((block, blockToPlace, type) -> {
                    getBlocks().add(block);
                    // block.setMetadata("building", new BuildData(building));
                    if (ItemUtils.SUITABLE_CONTAINER_TYPES.contains(type)) {
                        getChestsLocation().add(block.getLocation());
                    }
                    getOriginalBlockData().put(block.getLocation(), block.getBlockData().clone());
                    getOriginalBlocks().put(block.getLocation(), block.getType());
                    return type;
                }));

                pasters.add(coiPaster);

                Entry.getBuilder().pasteStructure(coiPaster, building);

                if (!iterator.hasNext()) {
                    this.cancel();
                }

            }
        }.runTaskTimer(Entry.getInstance(), 1, 2);
        new BukkitRunnable() {
            @Override
            public void run() {

                if (task.isCancelled()) {
                    setComplete(pasters.stream().allMatch(COIPaster::isComplete));

                    if (isComplete()) {

                        LoggerUtils.debug("complete");
                        Bukkit.getScheduler().runTask(Entry.getInstance(), () -> {
                            buildSuccess(location, player);
                        });
                        this.cancel();
                    }
                }


            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 0L, 1L);
    }

    public abstract LineBuild cloneBuild();

    protected void processLine(List<Location> line) {
    }

    public abstract boolean pointCheck(Block block);

    @Override
    public boolean deductionResources(Player player) {
        return super.deductionResources(player);
    }
}
