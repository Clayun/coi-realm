package com.mcylm.coi.realm.tools.building;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIScoreType;
import com.mcylm.coi.realm.events.BuildingDamagedEvent;
import com.mcylm.coi.realm.events.BuildingDestroyedEvent;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.model.COIPaster;
import com.mcylm.coi.realm.model.COIStructure;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.*;
import com.mcylm.coi.realm.utils.rotation.Rotation;
import lombok.Data;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 建筑物结构
 */
@Data
public abstract class COIBuilding implements Serializable {

    // 是否可建造
    protected boolean available = false;

    // 是否建造完成
    protected boolean complete = false;

    // 是否"活"着
    protected boolean alive = true;

    // 建筑类型
    protected COIBuildingType type;

    // 所需消耗的材料
    protected int consume = 0;

    // 建造的当前建筑的玩家
    protected String buildPlayerName;

    // 建筑的全部方块
    protected List<COIBlock> remainingBlocks;

    // 地图中建筑的所有方块
    protected Set<Block> blocks = new HashSet<>();

    // 建筑所替换的原方块数据
    protected Map<Location, BlockData> originalBlockData = new ConcurrentHashMap<>();
    protected Map<Location, Material> originalBlocks = new ConcurrentHashMap<>();

    // 放置物品的箱子位置
    protected List<Location> chestsLocation = new ArrayList<>();

    // 所在世界名称
    protected String world;

    // 建筑基点
    protected Location location;

    // 炮口，防御塔类建筑才有的
    protected Location muzzle;

    // 总方块数量
    protected Integer totalBlocks;

    // 建筑等级
    protected int level = 1;

    // 最高等级
    protected Integer maxLevel = 1;

    // 最大建筑数量
    protected Integer maxBuild = 1;

    // 建筑等级对照建筑结构表
    // key为等级，value是建筑结构文件名称
    protected Map<Integer, String> buildingLevelStructure = new HashMap<>();

    // 皮肤等级对照建筑结构表
    // 默认为 null
    protected Map<Integer, String> currentSkinStructure = null;

    // 建筑生成的NPC创建器，不生成NPC就设置NULL
    protected List<COINpc> npcCreators = new ArrayList<>();

    // 建筑所属的队伍
    protected COITeam team;

    // 建筑血量
    protected AtomicInteger health = new AtomicInteger(getMaxHealth());

    // 建筑配置
    protected BuildingConfig config;

    // 悬浮字相关
    protected Map<Player, Hologram> holograms = new HashMap<>();
    protected Map<Player, AtomicInteger> hologramVisitors = new HashMap<>();

    protected static String getHealthBarText(double max, double current, int length) {
        double percent = current / max;
        StringBuilder text = new StringBuilder("§a建筑血量: ");
        int healthLength = Math.toIntExact(Math.round(length * percent));
        text.append("§e|".repeat(Math.max(0, healthLength)));
        text.append("§7|".repeat(Math.max(0, length - healthLength)));
        return text.toString();
    }

    /**
     * 首次建造建筑
     */
    public void build(Location location, Player player) {

        if (!isAvailable()) {
            return;
        }

        // 记录玩家
        this.buildPlayerName = player.getName();

        // 扣除玩家背包里的资源
        boolean b = deductionResources(player);

        if (!b) {
            LoggerUtils.sendMessage("背包里的资源不够，请去收集资源", player);
            return;
        }

        // 建筑开始就记录位置
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

        // 设置NPC所属小队
        getNpcCreators().forEach(npcCreator -> {
            npcCreator.setTeam(TeamUtils.getTeamByPlayer(player));
            npcCreator.setBuilding(this);
        });

        COIBuilding building = this;
        // 构造一个建造器
        COIPaster coiPaster = new COIPaster(false, getType().getUnit(), getType().getInterval()
                , location.getWorld().getName(), location,null
                , structure, false, TeamUtils.getTeamByPlayer(player).getType().getBlockColor()
                , getNpcCreators(), ((block, blockToPlace, type) -> {
            blocks.add(block);
            // block.setMetadata("building", new BuildData(building));
            if (ItemUtils.SUITABLE_CONTAINER_TYPES.contains(type)) {
                chestsLocation.add(block.getLocation());
            }
            originalBlockData.put(block.getLocation(), block.getBlockData().clone());
            originalBlocks.put(block.getLocation(), block.getType());
            return type;
        }));

        // 开始建造
        Entry.getBuilder().pasteStructure(coiPaster, player, building);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (coiPaster.isComplete()) {
                    // 监听建造状态
                    complete = coiPaster.isComplete();

                    Bukkit.getScheduler().runTask(Entry.getInstance(), () -> {
                        buildSuccess(location, player);
                    });
                    this.cancel();

                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 0L, 20L);
        List<COIBuilding> buildings = TeamUtils.getTeamByPlayer(player).getFinishedBuildings();
        if (!buildings.contains(building)) {
            buildings.add(building);
        }
    }

    /**
     * 系统自动建造，适用于AI阵营，还有基地建造
     * @param location
     * @param team
     * @param isBase
     */
    public void build(Location location, COITeam team,boolean isBase) {

        if (!isAvailable()) {
            return;
        }

        // 建筑开始就记录位置
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

        // 设置NPC所属小队
        getNpcCreators().forEach(npcCreator -> {
            npcCreator.setTeam(team);
            npcCreator.setBuilding(this);
        });

        COIBuilding building = this;
        // 构造一个建造器
        COIPaster coiPaster = new COIPaster(false, getType().getUnit(), getType().getInterval()
                , location.getWorld().getName(), location,null
                , structure, false, team.getType().getBlockColor()
                , getNpcCreators(), ((block, blockToPlace, type) -> {
            blocks.add(block);
            // block.setMetadata("building", new BuildData(building));
            if (ItemUtils.SUITABLE_CONTAINER_TYPES.contains(type)) {
                chestsLocation.add(block.getLocation());
            }
            originalBlockData.put(block.getLocation(), block.getBlockData().clone());
            originalBlocks.put(block.getLocation(), block.getType());
            return type;
        }));

        // 开始建造
        Entry.getBuilder().pasteStructure(coiPaster, null, building);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (coiPaster.isComplete()) {
                    // 监听建造状态
                    complete = coiPaster.isComplete();
                    Bukkit.getScheduler().runTask(Entry.getInstance(), () -> {

                        buildSuccess(location, null);
                        if(isBase){
                            setTeamSpawnLocation(coiPaster.getSpawnLocation(),team);
                        }

                    });
                    this.cancel();

                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 0L, 20L);
        List<COIBuilding> buildings = team.getFinishedBuildings();
        if (!buildings.contains(building)) {
            buildings.add(building);
        }
    }

    protected void setTeamSpawnLocation(Location location,COITeam team){
        team.setSpawner(location);
    }

    public void buildSuccess(Location location, Player player) {
        // 建筑成功可以放个烟花
        spawnFirework(location);
        // 玩家新增建造奖励
        if(getType().equals(COIBuildingType.WALL_NORMAL)
            || getType().equals(COIBuildingType.DOOR_NORMAL)
            || getType().equals(COIBuildingType.BRIDGE)
        ){
            // 基础设施奖励，桥，门，墙等
            getTeam().addScore(COIScoreType.BUILD_INFRASTRUCTURE,player);
        }else{
            getTeam().addScore(COIScoreType.BUILD,player);
        }
    }

    public void upgradeBuild(Player player) {

        // 升级建筑先关闭complete，否则可以重复搞
        complete = false;

        for (Block b : getBlocks()) {
            b.removeMetadata("building", Entry.getInstance());

        }
        /*
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

         */

        // blocks.clear();
        // originalBlocks.clear();
        // originalBlockData.clear();
        remainingBlocks.clear();
        // chestsLocation.clear();

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
        // 构造一个建造器
        COIPaster coiPaster = new COIPaster(false, getType().getUnit(), getType().getInterval()
                , location.getWorld().getName(), location,null
                , structure, false, getTeam().getType().getBlockColor()
                , npcCreators, ((block, blockToPlace, type) -> {
            getBlocks().add(block);

            if (block.getState() instanceof Container container && type != block.getType()) {

                for (ItemStack item : container.getInventory().getContents()) {

                    if (item != null) block.getWorld().dropItemNaturally(block.getLocation(), item);

                }
                chestsLocation.remove(block.getLocation());
            }

            // block.setMetadata("building", new BuildData(building));
            if (ItemUtils.SUITABLE_CONTAINER_TYPES.contains(type)) {
                chestsLocation.add(block.getLocation());
            }
            originalBlockData.putIfAbsent(block.getLocation(), block.getBlockData().clone());
            originalBlocks.putIfAbsent(block.getLocation(), block.getType());
            return type;
        }));
        // 开始建造
        Entry.getBuilder().pasteStructure(coiPaster, player, building);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (coiPaster.isComplete()) {
                    // 监听建造状态
                    complete = true;
                    Bukkit.getScheduler().runTask(Entry.getInstance(), () -> {

                        // 升级奖励
                        getTeam().addScore(COIScoreType.UPGRADE_BUILDING,player);
                        upgradeBuildSuccess();
                    });
                    this.cancel();

                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 0, 20L);

    }

    public void upgradeBuildSuccess() {

        getNpcCreators().forEach(COINpc::upgrade);
        getHealth().set(getMaxHealth());
    }

    /**
     * 通过等级获取建筑文件名称
     *
     * @return
     */
    public String getStructureByLevel() {

        // 如果设置了皮肤，就用皮肤
        if(getCurrentSkinStructure() != null){
            return getCurrentSkinStructure().get(getLevel());
        }

        // 否则用默认模板
        return getBuildingLevelStructure().get(getLevel());
    }

    /**
     * 通过建筑结构文件获取所有方块
     *
     * @param structure
     * @return
     */
    public List<COIBlock> getAllBlocksByStructure(COIStructure structure) {
        // 全部待建造的方块
        List<COIBlock> allBlocks = structure.getBlocks();

        // 建筑基点
        Location basicLocation = getLocation();

        List<COIBlock> needBuildBlocks = new ArrayList<>();

        // 根据建筑基点设置每个方块的真实坐标
        for (COIBlock coiBlock : allBlocks) {

            COIBlock newBlock = new COIBlock();
            newBlock.setX(coiBlock.getX() + basicLocation.getBlockX());
            newBlock.setY(coiBlock.getY() + basicLocation.getBlockY());
            newBlock.setZ(coiBlock.getZ() + basicLocation.getBlockZ());
            newBlock.setBlockData(coiBlock.getBlockData());
            newBlock.setMaterial(coiBlock.getMaterial());

            if ("AIR".equals(newBlock.getMaterial())) {
                //删除掉空气方块
            } else
                needBuildBlocks.add(newBlock);
        }

        return needBuildBlocks;
    }

    /**
     * 找指定的方块位置
     * @param material
     * @return
     */
    public Location getBlockLocationByMaterial(String material) {

        String structureName = getStructureByLevel();

        if (structureName == null) {
            return null;
        }
        // 实例化建筑结构
        COIStructure structure = Entry.getBuilder().getStructureByFile(structureName);

        // 设置名称
        structure.setName(getType().getName());

        structure = prepareStructure(structure, location.clone());

        // 全部待建造的方块
        List<COIBlock> allBlocks = structure.getBlocks();

        // 建筑基点
        Location basicLocation = getLocation();

        // 根据建筑基点设置每个方块的真实坐标
        for (COIBlock coiBlock : allBlocks) {

            COIBlock newBlock = new COIBlock();
            newBlock.setX(coiBlock.getX() + basicLocation.getBlockX());
            newBlock.setY(coiBlock.getY() + basicLocation.getBlockY());
            newBlock.setZ(coiBlock.getZ() + basicLocation.getBlockZ());
            newBlock.setBlockData(coiBlock.getBlockData());
            newBlock.setMaterial(coiBlock.getMaterial());

            if (material.equals(newBlock.getMaterial())) {
                return new Location(location.getWorld(),newBlock.getX(),newBlock.getY(),newBlock.getZ());
            }
        }

        return null;
    }

    /**
     * 放个烟花
     * @param location
     */
    public void spawnFirework(Location location){
        // 检测到就放个烟花
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(getTeam().getType().getLeatherColor())
                .with(FireworkEffect.Type.BALL_LARGE)
                .build();
        Firework firework = (Firework) location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(effect);
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }


    // 找到箱子的位置
    protected List<Location> getChestsLocation(List<COIBlock> blocks) {

        List<Location> chestsLocations = new ArrayList<>();
        for (COIBlock block : blocks) {

            Material material = Material.getMaterial(block.getMaterial());

            if (material != null) {
                if (material.equals(Material.CHEST)) {
                    Location location = new Location(Bukkit.getWorld(getWorld()), block.getX(), block.getY(), block.getZ());
                    chestsLocations.add(location);
                }
            }


        }

        return chestsLocations;
    }

    /**
     * 根据建筑所需资源，扣除玩家背包的物品
     *
     * @param player
     * @return
     */
    public boolean deductionResources(Player player) {
        return InventoryUtils.deductionResources(player, getConsume());
    }

    /**
     * 获取玩家背包里的资源
     *
     * @return
     */
    public int getPlayerHadResource(Player player) {

        @NonNull ItemStack[] contents =
                player.getInventory().getContents();

        Material material = getResourceType();
        if (material == null) {
            return 0;
        }

        int num = 0;

        for (ItemStack itemStack : contents) {

            if (itemStack == null) {
                continue;
            }

            if (itemStack.getType().equals(material)) {
                num = num + itemStack.getAmount();
            }
        }

        return num;

    }

    public Material getResourceType() {
        String materialName = Entry.getInstance().getConfig().getString("game.building.material");

        return Material.getMaterial(materialName);

    }

    public int getMaxHealth() {
        return 100;
    }

    public void damage(Entity attacker, int damage, Block attackBlock) {
        if (!isComplete()) {
            return;
        }

        // 触发事件
        BuildingDamagedEvent event = new BuildingDamagedEvent(this,attackBlock,attacker);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (damage >= getHealth().get()) {
            getHealth().set(0);
            destroy(true);
            getTeam().addScore(COIScoreType.DESTROY_BUILDING,attacker);
            // 检查小队是否落败
            checkDefeat(attacker);
        } else {
            getHealth().addAndGet(-damage);
        }
        for (Entity e : location.getNearbyEntities(30, 20, 20)) {
            if (e instanceof Player p) {
                displayHealth(p);
            }
        }
    }

    /**
     * 修复建筑血量
     * @param health
     */
    public void repair(int health) {
        if (!isComplete()) {
            return;
        }

        if ((getHealth().get() + health) >= getMaxHealth()) {
            getHealth().set(getMaxHealth());
        } else {
            getHealth().getAndAdd(health);
        }

        for (Entity e : location.getNearbyEntities(30, 20, 20)) {
            if (e instanceof Player p) {
                displayHealth(p);
            }
        }
    }

    protected void checkDefeat(Entity attacker){
        // 击败小队的
        COITeam defeatedByTeam = null;
        Player player = null;
        // 判断是否是大本营
        if(getType().equals(COIBuildingType.BASE)){
            // 如果是大本营被拆除
            // 所属小队就判定失败
            if (attacker.getType().equals(EntityType.PLAYER)) {
                Player p = (Player)attacker;

                // 先把拆除建筑的当玩家
                COITeam teamByPlayer = TeamUtils.getTeamByPlayer(p);
                if(null != teamByPlayer){
                    defeatedByTeam = teamByPlayer;
                    player = p;
                }else{
                    COITeam npcTeam = TeamUtils.getNPCTeam(attacker);
                    if(null != npcTeam){
                        // 把拆除建筑的当NPC
                        defeatedByTeam = npcTeam;
                        COINpc npc = COINpc.getNPCByEntity(attacker);
                        if(npc != null){
                            String followPlayerName = npc.getFollowPlayerName();
                            Player followPlayer = Bukkit.getPlayer(followPlayerName);
                            if(followPlayer!= null
                                && followPlayer.isOnline()){
                                player = followPlayer;
                            }
                        }
                    }
                }

            }
            getTeam().defeatedBy(player,defeatedByTeam);
        }
    }

    public COIStructure prepareStructure(COIStructure structure, Location loc) {
        loc.setYaw(loc.getYaw() + 90);
        int n = Math.round(loc.getYaw() / 90);
        if (n == 1) {
            n = 3;
        } else if (n == 3) {
            n = 1;
        } else if (n == 4) { // 强制0度
            n = 0;
        }

        structure.rotate(Rotation.fromDegrees(n * 90));
        return structure;
    }

    public void displayHealth(Player p) {
        if (hologramVisitors.containsKey(p)) {
            hologramVisitors.get(p).set(5);
        } else {
            hologramVisitors.put(p, new AtomicInteger(5));
        }
        if (!holograms.containsKey(p)) {
            Hologram hologram = HolographicDisplaysAPI.get(Entry.getInstance()).createHologram(location);
            hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
            hologram.getVisibilitySettings().setIndividualVisibility(p, VisibilitySettings.Visibility.VISIBLE);
            hologram.getLines().appendText(String.format(LoggerUtils.replaceColor(team.getType().getColor() + "%s Lv. %s"), type.getName(), getLevel()));
            @NotNull TextHologramLine line = hologram.getLines().appendText(getHealthBarText(getMaxHealth(), getHealth().get(), getHealthBarLength()));

            holograms.put(p, hologram);
            new BukkitRunnable() {
                int tick = 0;

                @Override
                public void run() {
                    if (!holograms.containsKey(p)) {
                        Entry.runSync(hologram::delete);
                        this.cancel();
                    } else {
                        if (tick++ == 20) {
                            tick = 0;

                            if(hologramVisitors.get(p) != null){
                                if (hologramVisitors.get(p).decrementAndGet() == 0) {
                                    holograms.remove(p);
                                    hologramVisitors.remove(p);
                                }
                            }
                        }
                        int maxDistance = 12;
                        List<Location> loc = LocationUtils.line(getHologramPoint(), p.getEyeLocation(), 1);
                        int distance = loc.size();
                        if (maxDistance < distance) {
                            distance = maxDistance;
                        }
                        int finalDistance = loc.size() >= 3 ? distance - 3 : loc.size() ;

                        Entry.runSync(() -> {
                            if (hologram.isDeleted()) return;
                            line.setText(getHealthBarText(getMaxHealth(), getHealth().get(), getHealthBarLength()));

                            if(loc.size() > finalDistance){
                                hologram.setPosition(loc.get(finalDistance).add(0,0.5,0));
                            }
                        });
                    }
                }
            }.runTaskTimerAsynchronously(Entry.getInstance(), 1, 1);

        }
    }

    public int getUpgradeRequiredConsume() {
        return consume + level * 80;
    }

    /**
     * 拆除返还的资源
     * @return
     */
    public int getDestroyReturn() {
        // 优化算法
        return Math.toIntExact(Math.round((consume + (level - 1) * 80) * 0.8));
    }

    public void destroy(boolean effect) {
        if (!isComplete()) {
            return;
        }

        BuildingDestroyedEvent event = new BuildingDestroyedEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        for (Hologram value : holograms.values()) {
            value.delete();
        }
        holograms.clear();

        Set<Map.Entry<Location, Material>> blocks = getOriginalBlocks().entrySet();
        Set<Map.Entry<Location, BlockData>> blockData = getOriginalBlockData().entrySet();
        for (Block b : getBlocks()) {
            b.removeMetadata("building", Entry.getInstance());
            if (Math.random() > 0.8 && effect) {
                b.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, b.getLocation(), 1);
            }
            FallingBlock fallingBlock = b.getWorld().spawnFallingBlock(b.getLocation().add(0.5, 0.5, 0.5), b.getBlockData());
            fallingBlock.setDropItem(false);
            fallingBlock.setHurtEntities(false);
            fallingBlock.setMetadata("break_falling_block", new FixedMetadataValue(Entry.getInstance(), "fake_block"));
        }
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
        npcCreators.forEach(COINpc::remove);

        setAlive(false);
        team.getFoodChests().removeAll(getChestsLocation());

        // 掉落建造成本的一部分
        int returnResource = getDestroyReturn();
        int group = returnResource / 64;
        int amount = returnResource % 64;
        Material material = getResourceType();
        for (int i = 0; i < group; i++) {
            getLocation().getWorld().dropItemNaturally(getLocation(), new ItemStack(material, 64));
        }
        getLocation().getWorld().dropItemNaturally(getLocation(), new ItemStack(material, amount));
    }

    public Location getHologramPoint() {
        return getLocation();
    }

    protected int getHealthBarLength() {
        return 20;
    }

    public void upgrade(Player player) {
        if (level + 1 > maxLevel || !isComplete()) {
            return;
        }

        // 扣减资源，扣除成功就是 true ，不够就不扣，返回 false
        boolean b = InventoryUtils.deductionResources(player, getUpgradeRequiredConsume());
        if (b) {
            level++;
            upgradeBuild(player);
        }else{
            LoggerUtils.sendMessage("背包里的资源不够，请去收集资源", player);
        }
    }

    public Block getNearestBlock(Location location) {
        List<Block> blocks = new ArrayList<>(this.blocks);
        blocks.sort(Comparator.comparingDouble(b -> location.distance(b.getLocation())));
        return blocks.get(0);
    }

    public abstract BuildingConfig getDefaultConfig();

    protected void applyConfig() {
        consume = config.getConsume();
        maxLevel = config.getMaxLevel();
        maxBuild = config.getMaxBuild();
        setBuildingLevelStructure(config.getStructures());
    }

    public void setConfig(BuildingConfig config){
        this.config = config;
        applyConfig();
    }
}
