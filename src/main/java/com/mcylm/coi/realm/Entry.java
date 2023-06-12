package com.mcylm.coi.realm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcylm.coi.realm.cmd.COIStructureCommand;
import com.mcylm.coi.realm.cmd.DebugCommand;
import com.mcylm.coi.realm.cmd.VeinCommand;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COIServerMode;
import com.mcylm.coi.realm.game.COIGame;
import com.mcylm.coi.realm.listener.GameListener;
import com.mcylm.coi.realm.listener.MineralsBreakListener;
import com.mcylm.coi.realm.listener.PlayerInteractListener;
import com.mcylm.coi.realm.managers.COIBuildingManager;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.impl.*;
import com.mcylm.coi.realm.tools.building.impl.monster.COIMonsterBase;
import com.mcylm.coi.realm.tools.data.MapData;
import com.mcylm.coi.realm.tools.data.metadata.EntityData;
import com.mcylm.coi.realm.tools.npc.impl.COISoldier;
import com.mcylm.coi.realm.tools.npc.impl.monster.COIMonster;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Scoreboard;

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


        // 开发测试环境注册
        if (serverMode.equals(COIServerMode.DEVELOP)) {
            //注册建筑结构相关的命令
            getCommand("structure").setExecutor(new COIStructureCommand());
            getCommand("cdebug").setExecutor(new DebugCommand());
            getCommand("cvein").setExecutor(new VeinCommand());
            LoggerUtils.log("命令注册完成");
        }

        registerEventListeners();
        registerDefaultBuildings();

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
    }

    private void registerEventListeners() {
        // 注册监听器
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerInteractListener(), this);
        pluginManager.registerEvents(new GameListener(), this);
        pluginManager.registerEvents(new MineralsBreakListener(), this);
        // AI事件监听器
        COISoldier.registerListener();
        COIMonster.registerListener();

        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    if(Entry.getGame().getStatus().equals(COIGameStatus.WAITING)){
                        // 等待中，就初始化背包
                        Entry.getGame().initPlayerWaiting(e.getPlayer());
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
                .filter(e -> e.getPlayer().isSneaking())
                .filter(e -> {
                    COINpc npc = EntityData.getNpcByEntity(e.getRightClicked());
                    if (npc != null) {
                        return TeamUtils.getTeamByPlayer(e.getPlayer()) == npc.getTeam();
                    }
                    return false;
                })
                .handler(e -> {
                    Inventory inv = EntityData.getNpcByEntity(e.getRightClicked()).getInventory();

                    e.getPlayer().openInventory(inv);
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
}
