package com.mcylm.coi.realm.tools.building;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.model.COIPaster;
import com.mcylm.coi.realm.model.COIStructure;
import com.mcylm.coi.realm.tools.building.data.BuildData;
import com.mcylm.coi.realm.tools.npc.AI;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import lombok.Setter;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 建筑物结构
 */
@Setter
@Getter
public class COIBuilding implements Serializable {

    // 是否可建造
    private boolean available = false;

    // 是否建造完成
    private boolean complete = false;

    // 建筑类型
    private COIBuildingType type;

    // 所需消耗的材料
    private int consume = 0;

    // 建筑的全部方块
    private List<COIBlock> remainingBlocks;

    // 地图中建筑的所有方块
    private Set<Block> blocks = new HashSet<>();

    // 建筑所替换的原方块数据
    private Map<Location, BlockData> originalBlockData = new HashMap<>();
    private Map<Location, Material> originalBlocks = new HashMap<>();

    // 放置物品的箱子位置
    private List<Location> chestsLocation;

    // 所在世界名称
    private String world;

    // 建筑基点
    private Location location;

    // 总方块数量
    private Integer totalBlocks;

    // 建筑等级
    private int level = 1;

    // 最高等级
    private Integer maxLevel = 1;

    // 建筑等级对照建筑结构表
    // key为等级，value是建筑结构文件名称
    private HashMap<Integer, String> buildingLevelStructure = new HashMap<>();

    // 建筑生成的NPC创建器，不生成NPC就设置NULL
    private COINpc npcCreator;

    // 建筑所属的队伍
    private COITeam team;

    // 建筑血量
    private AtomicInteger health = new AtomicInteger(getMaxHealth());

    // 悬浮字相关
    private @Nullable Hologram hologram;
    private Map<Player, AtomicInteger> hologramVisitors = new HashMap<>();
    /**
     * 首次建造建筑
     */
    public void build(Location location, Player player){

        if(!isAvailable()){
            return;
        }

        // 扣除玩家背包里的资源
        boolean b = deductionResources(player);

        if(!b){
            LoggerUtils.sendMessage("背包里的资源不够，请去收集资源",player);
            return;
        }

        // 建筑开始就记录位置
        setLocation(location);
        setWorld(location.getWorld().getName());

        String structureName = getStructureByLevel();

        if (structureName == null) {
            return;
        }
        // 实例化建筑结构
        COIStructure structure = Entry.getBuilder().getStructureByFile(structureName);

        // 预先计算建筑的方块位置，及总方块数量
        List<COIBlock> allBlocks = getAllBlocksByStructure(structure);
        setRemainingBlocks(allBlocks);
        setTotalBlocks(allBlocks.size());

        // 设置箱子的位置
        List<Location> chestsLocation = getChestsLocation(allBlocks);
        setChestsLocation(chestsLocation);

        // 设置名称
        structure.setName(getType().getName());

        structure = prepareStructure(structure, player);
        // 设置NPC所属小队
        if (getNpcCreator() != null) {
            getNpcCreator().setTeam(TeamUtils.getTeamByPlayer(player));
        }

        COIBuilding building = this;
        // 构造一个建造器
        COIPaster coiPaster = new COIPaster(false,getType().getUnit(),getType().getInterval()
                  ,location.getWorld().getName(),location
                ,structure,false, TeamUtils.getTeamByPlayer(player).getType().getBlockColor()
                ,getNpcCreator(), ((block, blockToPlace, type) -> {
                    blocks.add(block);
                    block.setMetadata("building", new BuildData(building));
                    originalBlockData.put(block.getLocation(), block.getBlockData().clone());
                    originalBlocks.put(block.getLocation(), block.getType());
                    if (type == getHologramReplaceMaterial()) {
                        createHologram(block.getLocation());
                        return Material.AIR;
                    }
                    return type;
        }));

        // 开始建造
        Entry.getBuilder().pasteStructure(coiPaster,player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(coiPaster.isComplete()){
                    // 监听建造状态
                    complete = coiPaster.isComplete();
                    Bukkit.getScheduler().runTask(Entry.getInstance(), () -> {
                        buildSuccess(location, player);
                    });
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(),0L,20L);
    }

    protected Hologram createHologram(Location l) {
        Hologram hologram = HolographicDisplaysAPI.get(Entry.getInstance()).createHologram(l.add(0.5,0.5,0.5));

        hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
        hologram.getLines().appendText(getHealthText(getMaxHealth(), getMaxHealth()));
        new BukkitRunnable() {
            @Override
            public void run() {

                for (Map.Entry<Player, AtomicInteger> entry : hologramVisitors.entrySet()) {

                    if (entry.getValue().decrementAndGet() <= 0) {
                        hologram.getVisibilitySettings().setIndividualVisibility(entry.getKey(), VisibilitySettings.Visibility.HIDDEN);
                    }
                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 20, 20);

        this.hologram = hologram;
        return hologram;
    }

    protected Material getHologramReplaceMaterial() {
        return Material.COMMAND_BLOCK;
    }

    public COIStructure prepareStructure(COIStructure structure, Player player) {
        return structure;
    }

    public void buildSuccess(Location location, Player player) {}


        /**
         * 通过等级获取建筑文件名称
         * @return
         */
        public String getStructureByLevel(){
        return getBuildingLevelStructure().get(getLevel());
    }

    /**
     * 通过建筑结构文件获取所有方块
     * @param structure
     * @return
     */
    public List<COIBlock> getAllBlocksByStructure(COIStructure structure){
        // 全部待建造的方块
        List<COIBlock> allBlocks = structure.getBlocks();

        // 建筑基点
        Location basicLocation = getLocation();

        List<COIBlock> needBuildBlocks = new ArrayList<>();

        // 根据建筑基点设置每个方块的真实坐标
        for(COIBlock coiBlock : allBlocks){

            COIBlock newBlock = new COIBlock();
            newBlock.setX(coiBlock.getX() + basicLocation.getBlockX());
            newBlock.setY(coiBlock.getY() + basicLocation.getBlockY());
            newBlock.setZ(coiBlock.getZ() + basicLocation.getBlockZ());
            newBlock.setBlockData(coiBlock.getBlockData());
            newBlock.setMaterial(coiBlock.getMaterial());

            if("AIR".equals(newBlock.getMaterial())){
                //删除掉空气方块
            }else
                needBuildBlocks.add(newBlock);
        }

        return needBuildBlocks;
    }

    // 找到箱子的位置
    private List<Location> getChestsLocation(List<COIBlock> blocks){

        List<Location> chestsLocations = new ArrayList<>();
        for(COIBlock block : blocks){

            Material material = Material.getMaterial(block.getMaterial());

            if(material != null){
                if(material.equals(Material.CHEST)){
                    Location location = new Location(Bukkit.getWorld(getWorld()),block.getX(),block.getY(),block.getZ());
                    chestsLocations.add(location);
                }
            }


        }

        return chestsLocations;
    }

    /**
     * 根据建筑所需资源，扣除玩家背包的物品
     * @param player
     * @return
     */
    public boolean deductionResources(Player player){

        int playerHadResource = getPlayerHadResource(player);

        // 如果玩家手里的资源数量足够
        if(playerHadResource >= getConsume()){

            // 扣减物品
            ItemStack[] contents =
                    player.getInventory().getContents();

            // 剩余所需扣减资源数量
            int deductionCount = getConsume();

            String materialName = Entry.getInstance().getConfig().getString("game.building.material");

            // 资源类型
            Material material = Material.getMaterial(materialName);

            for(ItemStack itemStack : contents){

                if(itemStack == null){
                    continue;
                }

                // 是资源物品才扣减
                if(itemStack.getType().equals(material)){
                    // 如果当前物品的堆叠数量大于所需资源，就只扣减数量
                    if(itemStack.getAmount() > deductionCount){
                        itemStack.setAmount(itemStack.getAmount() - deductionCount);
                        return true;
                    }

                    // 如果当前物品的堆叠数量等于所需资源，就删物品
                    if(itemStack.getAmount() == deductionCount){
                        player.getInventory().removeItem(itemStack);
                        player.updateInventory();
                        return true;
                    }

                    // 如果物品的堆叠数量小于所需资源，就删物品，同时计数
                    if(itemStack.getAmount() < deductionCount){
                        // 减去当前物品的库存
                        deductionCount = deductionCount - itemStack.getAmount();
                        player.getInventory().removeItem(itemStack);
                        player.updateInventory();
                    }
                }



            }

        }else
            return false;

        return false;
    }

    /**
     * 获取玩家背包里的资源
     * @return
     */
    public int getPlayerHadResource(Player player){

        @NonNull ItemStack[] contents =
                player.getInventory().getContents();

        String materialName = Entry.getInstance().getConfig().getString("game.building.material");

        Material material = Material.getMaterial(materialName);

        if(material == null){
            return 0;
        }

        int num = 0;

        for(ItemStack itemStack : contents){

            if(itemStack == null){
                continue;
            }

            if(itemStack.getType().equals(material)){
                num = num + itemStack.getAmount();
            }
        }

        return num;

    }

    public int getMaxHealth() {
        return 100;
    }

    public void damage(Entity attacker, int damage, Block attackBlock) {
        if (damage >= getHealth().get()) {
            getHealth().set(0);
            destroy();
        } else {
            getHealth().addAndGet(-damage);
        }
        for (Entity e : location.getNearbyEntities(20, 20, 20)) {
            if (e instanceof Player p) {
                displayHealth(p);
            }
        }
        if (hologram != null) {
            if (hologram.getLines().get(0) instanceof TextHologramLine line) {
                line.setText(getHealthText(getMaxHealth(), getHealth().get()));
            }
        }
    }

    public void displayHealth(Player p) {
        if (hologram == null) return;
        hologram.getVisibilitySettings().setIndividualVisibility(p, VisibilitySettings.Visibility.VISIBLE);
        if (hologramVisitors.containsKey(p)) {
            hologramVisitors.get(p).set(5);
        } else {
            hologramVisitors.put(p, new AtomicInteger(5));
        }
    }

    public void destroy() {
        if (hologram != null) {
            hologram.delete();
        }
        for (Block b : getBlocks()) {
            b.removeMetadata("building", Entry.getInstance());
            if (Math.random() > 0.8) {
                b.getWorld().spawnParticle(Particle.SMOKE_LARGE, b.getLocation(), 2, 2);
            }
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
        complete = false;
        team.getFinishedBuildings().remove(this);
        if (npcCreator != null) npcCreator.remove();
    }

    private static String getHealthText(double max, double current) {
        double percent = current / max;
        int length = 15;
        StringBuilder text = new StringBuilder("§a建筑血量");
        int healthLength = Math.toIntExact(Math.round(length * percent));
        text.append("§e|".repeat(Math.max(0, healthLength)));
        text.append("§7|".repeat(Math.max(0, length - healthLength)));
        return text.toString();
    }

}
