package com.mcylm.coi.realm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcylm.coi.realm.cmd.AllCommand;
import com.mcylm.coi.realm.cmd.COIStructureCommand;
import com.mcylm.coi.realm.cmd.DebugCommand;
import com.mcylm.coi.realm.cmd.VeinCommand;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COIServerMode;
import com.mcylm.coi.realm.game.COIGame;
import com.mcylm.coi.realm.gui.ForgeGUI;
import com.mcylm.coi.realm.item.COIRocket;
import com.mcylm.coi.realm.listener.GameListener;
import com.mcylm.coi.realm.listener.MineralsBreakListener;
import com.mcylm.coi.realm.listener.PlayerInteractListener;
import com.mcylm.coi.realm.listener.SnowballCoolDownListener;
import com.mcylm.coi.realm.managers.COIBuildingManager;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.player.COIPlayer;
import com.mcylm.coi.realm.tools.attack.impl.PatrolGoal;
import com.mcylm.coi.realm.tools.attack.impl.TeamFollowGoal;
import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.target.impl.EntityTarget;
import com.mcylm.coi.realm.tools.attack.team.AttackTeam;
import com.mcylm.coi.realm.tools.building.impl.*;
import com.mcylm.coi.realm.tools.building.impl.monster.COIMonsterBase;
import com.mcylm.coi.realm.tools.data.MapData;
import com.mcylm.coi.realm.tools.data.metadata.EntityData;
import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import com.mcylm.coi.realm.tools.npc.impl.COISoldier;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.MapUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.citizensnpcs.api.CitizensAPI;
import net.kyori.adventure.text.Component;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Entry extends ExtendedJavaPlugin {
    // 插件实例
    private static Entry instance;

    // COIBuilder 实例，控制建筑自动建造
    private static COIBuilder builder;

    // 插件名称
    public static String PREFIX = "岛屿冲突 Realm";

    // 本插件的文件目录
    public static String PLUGIN_FILE_PATH;

    // 服务器模式，在配置文件 Config 中有详细注释
    public static String SERVER_MODE = "develop";

    // 游戏世界
    public static String WORLD;

    // 每个小队最大人数限制
    public static Integer MAX_GROUP_PLAYERS = 1;

    // NPC可食用的 Material Name
    private static List<String> NPC_FOODS;

    // 城墙根部坐标检测起始高度
    public static Integer WALL_DETECT_HEIGHT = 18;

    // 建筑升级中跳过的方块
    public static List<String> UPGRADE_SKIP_BLOCKS;
    // 主游戏进程管理
    private static COIGame game;

    // 地图数据
    @Getter
    private static MapData mapData;

    // GSON
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private COIBuildingManager buildingManager = new COIBuildingManager();

    private File mapDataFile = new File(getDataFolder(), "map.json");

    // 计分板
    @Getter
    private Scoreboard scoreboard;

    @Override
    protected void enable() {
        instance = this;
        builder = new COIBuilder();
        NPC_FOODS = new ArrayList<>();
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        LoggerUtils.log(Entry.getInstance().getName() + " 开始加载...");

        PLUGIN_FILE_PATH = "plugins/" + Entry.getInstance().getName() + "/";

        if (!new File(PLUGIN_FILE_PATH).exists()) {
            new File(PLUGIN_FILE_PATH).mkdir();
            LoggerUtils.log("已成功创建基础配置文件夹");
        }

        SERVER_MODE = getConfig().getString("server-mode");
        MAX_GROUP_PLAYERS = getConfig().getInt("game.max-group-players");
        NPC_FOODS = getConfig().getStringList("foods");
        WALL_DETECT_HEIGHT = getConfig().getInt("game.wall-detect-height");
        UPGRADE_SKIP_BLOCKS = getConfig().getStringList("upgrade-skip-blocks");
        WORLD = Entry.getInstance().getConfig().getString("game.spawn-world");
        COIServerMode serverMode = COIServerMode.parseCode(SERVER_MODE);

        if (serverMode == null) {
            LoggerUtils.log("服务器模式识别失败！");
            this.getServer().shutdown();
        }

        LoggerUtils.log("当前插件模式：" + serverMode.getName());

        saveDefaultConfig();
        if (!mapDataFile.exists()) {
            saveMapData();
        }
        readMapData();

        // 注册主服务器
        if(getConfig().getBoolean("bungeecord")){
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        }

        // 开发测试环境注册
        if (serverMode.equals(COIServerMode.DEVELOP)) {
            //注册建筑结构相关的命令
            getCommand("structure").setExecutor(new COIStructureCommand());
            getCommand("cdebug").setExecutor(new DebugCommand());
            getCommand("cvein").setExecutor(new VeinCommand());
            LoggerUtils.log("命令注册完成");
        }

        // 正常环境命令注册
        getCommand("all").setExecutor(new AllCommand());

        // 团队数量
        int maxTeams = Entry.getInstance().getConfig().getInt("game.max-teams");
        // 每个团队最大人数
        int maxPlayerPerTeam = Entry.getInstance().getConfig().getInt("game.max-group-players");
        // 服务器最大在线人数
        int maxPlayer = maxPlayerPerTeam * maxTeams;
        // 设置最大在线人数
        getServer().setMaxPlayers(maxPlayer);

        // 检测前置插件是否加载了
        // 检查是否已加载Disguise插件
        try {
            Class.forName("me.libraryaddict.disguise.DisguiseAPI");
        } catch (ClassNotFoundException e) {
            // 如果没有加载Disguise插件，则重新加载服务器
            Bukkit.getServer().reload();
            return;
        }

        registerEventListeners();
        registerDefaultBuildings();

        // 展示游戏公告
        showNotice();

        // 一切准备就绪，创建主游戏进程
        game = new COIGame();
        LoggerUtils.log("小游戏主线程创建完成");

        // 游戏开始
        game.start();

    }

    @Override
    protected void disable() {

        // 全T掉，防止boss bar叠加显示
        for (Player p : getServer().getOnlinePlayers()) {
            p.kick(Component.text("服务器重载中,请稍后重连"));
        }

        // 删除NPC
        CitizensAPI.getNPCRegistry().deregisterAll();

        // 清空Team的缓存
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
    }

    /**
     * 自动展示公告
     */
    private void showNotice(){
        List<String> notice = getConfig().getStringList("notice");

        if(notice.isEmpty()){
            return;
        }

        // 间隔
        int interval = getConfig().getInt("notice-interval");

        if(interval == 0){
            interval = 10;
        }

        new BukkitRunnable(){

            int index = 0;
            @Override
            public void run() {
                // 每次循环播报一条公告信息
                String announcement = notice.get(index);

                LoggerUtils.broadcastMessage(announcement);

                // 将索引加 1，如果已经到达列表末尾，则重新开始循环
                index++;
                if (index >= notice.size()) {
                    index = 0;
                }
            }
        }.runTaskTimerAsynchronously(this,0,20 * interval);
    }

    private void registerEventListeners() {
        // 注册监听器
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerInteractListener(), this);
        pluginManager.registerEvents(new SnowballCoolDownListener(), this);
        pluginManager.registerEvents(new GameListener(), this);
        pluginManager.registerEvents(new MineralsBreakListener(), this);
        // AI事件监听器
        COISoldier.registerListener();
        // 助推器监听器
        COIRocket.registerListener();

        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    if(Entry.getGame().getStatus().equals(COIGameStatus.WAITING)){
                        // 等待中，就初始化背包
                        Entry.getGame().initPlayerWaiting(e.getPlayer());

                    }
        });


        Events.subscribe(EntityDamageByEntityEvent.class)
                .handler(e-> {
                    if (e.getDamager() instanceof Player p && e.getEntity() instanceof LivingEntity livingEntity) {
                        COIPlayer coiPlayer = Entry.getGame().getCOIPlayer(p);
                        AttackTeam team = coiPlayer.getAttackTeam();
                        if (team.getStatus() == AttackTeam.Status.LOCK) {
                            for (COIEntity entity : coiPlayer.getAttackTeam().getMembers()) {
                                if (entity instanceof COISoldier soldier && soldier.isAlive()) {
                                    Target target = new EntityTarget(livingEntity, 9);
                                    soldier.setTarget(target);
                                }
                            }
                        }
                    }

                });

        Events.subscribe(ProjectileHitEvent.class)
                .handler(e -> {
                    if (e.getHitEntity() != null && e.getHitEntity().hasMetadata("preview_block")) {
                        e.setCancelled(true);
                    }
        });

        Events.subscribe(EntityChangeBlockEvent.class)
                .handler(e -> {
                    if (e.getEntity().getType() == EntityType.FALLING_BLOCK && e.getEntity().hasMetadata("break_falling_block")) {
                        e.setCancelled(true);
                        e.getEntity().removeMetadata("break_falling_block", getInstance());
                        e.getEntity().remove();
                    }
                });

        Events.subscribe(PlayerInteractAtEntityEvent.class)
                .filter(e -> {
                    COINpc npc = EntityData.getNpcByEntity(e.getRightClicked());
                    if (npc != null) {
                        return TeamUtils.getTeamByPlayer(e.getPlayer()) == npc.getTeam();
                    }
                    return false;
                })
                .handler(e -> {

                    COINpc npcByEntity = EntityData.getNpcByEntity(e.getRightClicked());

                    Inventory inv = npcByEntity.getInventory();

                    if(npcByEntity.getBuilding().getType().equals(COIBuildingType.FORGE)){
                        // 铁匠铺打开另一个GUI
                        new ForgeGUI(e.getPlayer(),npcByEntity.getBuilding());
                    }else{
                        if (!e.getPlayer().isSneaking()) {
                            e.getPlayer().openInventory(inv);
                        } else {
                            COIPlayer coiPlayer = Entry.getGame().getCOIPlayer(e.getPlayer());

                            if (e.getHand().equals(EquipmentSlot.HAND)) {

                                if (npcByEntity instanceof COISoldierCreator creator) {
                                    COISoldier soldier = ((COISoldier) creator.getNpc());
                                    ;
                                    if (creator.getAttackTeam() == null || creator.getAttackTeam().getCommander().isDead()) {
                                        coiPlayer.getAttackTeam().getMembers().add((COIEntity) creator.getNpc());
                                        creator.setAttackTeam(coiPlayer.getAttackTeam());
                                        soldier.setGoal(new TeamFollowGoal(soldier, coiPlayer.getAttackTeam()));
                                        soldier.getGoal().start();
                                        LoggerUtils.sendMessage("&a成功入队", e.getPlayer());
                                    } else if (creator.getAttackTeam() == coiPlayer.getAttackTeam()) {
                                        coiPlayer.getAttackTeam().getMembers().remove(soldier);
                                        creator.setAttackTeam(null);
                                        soldier.setGoal(new PatrolGoal(soldier));
                                        soldier.getGoal().start();
                                        LoggerUtils.sendMessage("&c成功脱队", e.getPlayer());
                                    }
                                }

                            }
                        }
                    }

                    if (inv != null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (e.getRightClicked().isDead() || e.getPlayer().getWorld() != e.getRightClicked().getWorld() || e.getPlayer().getLocation().distance(e.getRightClicked().getLocation()) > 6) {
                                    this.cancel();
                                    e.getPlayer().closeInventory();
                                }
                            }
                        }.runTaskTimer(getInstance(), 2, 3);
                    }
                });


        LoggerUtils.log("监听器注册完成");
    }

    private void registerDefaultBuildings() {
        buildingManager.registerBuilding(COIBuildingType.BASE, COIBase.class);
        buildingManager.registerBuilding(COIBuildingType.STOPE, COIStope.class);
        buildingManager.registerBuilding(COIBuildingType.MILL, COIMill.class);
        buildingManager.registerBuilding(COIBuildingType.MILITARY_CAMP, COICamp.class);
        buildingManager.registerBuilding(COIBuildingType.WALL_NORMAL, COIWall.class);
        buildingManager.registerBuilding(COIBuildingType.DOOR_NORMAL, COIDoor.class);
        buildingManager.registerBuilding(COIBuildingType.FORGE, COIForge.class);

        // 防御塔系列
        buildingManager.registerBuilding(COIBuildingType.TURRET_NORMAL, COITurret.class);
        buildingManager.registerBuilding(COIBuildingType.TURRET_REPAIR, COIRepair.class);
        buildingManager.registerBuilding(COIBuildingType.TURRET_AIR_RAID, COIAirRaid.class);

        // 怪物系列
        buildingManager.registerBuilding(COIBuildingType.MONSTER_BASE, COIMonsterBase.class);
        LoggerUtils.log("建筑文件注册完成");
    }

    public static void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(instance, runnable);
    }

    public static Entry getInstance() {
        return instance;
    }

    public static COIBuilder getBuilder() {
        return builder;
    }

    public static List<String> getNpcFoods() {
        return NPC_FOODS;
    }

    public static COIGame getGame() {
        return game;
    }

    public void readMapData() {
        try (FileReader reader = new FileReader(mapDataFile)) {
            mapData = GSON.fromJson(reader, MapData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void saveMapData() {
        if (mapData == null) {
            mapData = new MapData();
        }
        try (FileWriter writer = new FileWriter(mapDataFile)) {
            GSON.toJson(mapData, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 重置小游戏地图
     * 无法实现，没有主目录的权限
     */
    @Deprecated
    private void resetMap(){

        String worldDirName = getConfig().getString("game.spawn-world");
        String zipName = getConfig().getString("game.world-zip");

        if(worldDirName == null || zipName == null){
            return;
        }
        // 先删除原本的地图
        deleteMapFolder(worldDirName);

        // 解压地图
        try {
            MapUtils.extractMap(
                    new File(getServer().getWorldContainer().getParentFile(), zipName),
                    new File(getServer().getWorldContainer().getParentFile(), worldDirName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteMapFolder(String name) {
        File folder = new File(getServer().getWorldContainer().getParentFile(), name);
        if (folder.exists()) {
            try {
                FileUtils.deleteDirectory(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
