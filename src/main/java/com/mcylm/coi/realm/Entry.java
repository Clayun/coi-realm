package com.mcylm.coi.realm;

import com.mcylm.coi.realm.cmd.COIStructureCommand;
import com.mcylm.coi.realm.cmd.DebugCommand;
import com.mcylm.coi.realm.enums.COIServerMode;
import com.mcylm.coi.realm.game.COIGame;
import com.mcylm.coi.realm.listener.MineralsBreakListener;
import com.mcylm.coi.realm.listener.PlayerInteractListener;
import com.mcylm.coi.realm.tools.building.impl.COIBuilder;
import com.mcylm.coi.realm.utils.LoggerUtils;
import lombok.Getter;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.io.File;
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

    // 主游戏进程管理
    private static COIGame game;

    @Override
    protected void enable() {

        instance = this;
        builder = new COIBuilder();
        NPC_FOODS = new ArrayList<>();

        LoggerUtils.log(Entry.getInstance().getName()+" 开始加载...");

        PLUGIN_FILE_PATH = "plugins/"+ Entry.getInstance().getName()+"/";

        if (!new File(PLUGIN_FILE_PATH).exists()) {
            new File(PLUGIN_FILE_PATH).mkdir();
            LoggerUtils.log("已成功创建基础配置文件夹");
        }

        SERVER_MODE = getConfig().getString("server-mode");
        MAX_GROUP_PLAYERS = getConfig().getInt("game.max-group-players");
        NPC_FOODS = getConfig().getStringList("foods");

        COIServerMode serverMode = COIServerMode.parseCode(SERVER_MODE);

        if(serverMode == null){
            LoggerUtils.log("服务器模式识别失败！");
            this.getServer().shutdown();
        }

        LoggerUtils.log("当前插件模式："+serverMode.getName());

        saveDefaultConfig();

        // 注册监听器
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerInteractListener(), this);

        if(serverMode.equals(COIServerMode.RELEASE)){
            pluginManager.registerEvents(new MineralsBreakListener(), this);
        }

        // 开发测试环境注册
        if(serverMode.equals(COIServerMode.DEVELOP)){
            //注册建筑结构相关的命令
            getCommand("structure").setExecutor(new COIStructureCommand());
            getCommand("cdebug").setExecutor(new DebugCommand());
        }

        // 一切准备就绪，创建主游戏进程
        game = new COIGame();

        // TODO 开始游戏

    }

    @Override
    protected void disable() {

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
}
