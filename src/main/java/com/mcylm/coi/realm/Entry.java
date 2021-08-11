package com.mcylm.coi.realm;

import com.mcylm.coi.realm.cmd.COIStructureCommand;
import com.mcylm.coi.realm.enums.COIServerMode;
import com.mcylm.coi.realm.listener.PlayerInteractListener;
import com.mcylm.coi.realm.tools.COIBuilder;
import com.mcylm.coi.realm.utils.LoggerUtils;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.PluginManager;

import java.io.File;

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

    @Override
    protected void enable() {

        instance = this;
        builder = new COIBuilder();

        LoggerUtils.log(Entry.getInstance().getName()+" 开始加载...");

        PLUGIN_FILE_PATH = "plugins/"+ Entry.getInstance().getName()+"/";

        if (!new File(PLUGIN_FILE_PATH).exists()) {
            new File(PLUGIN_FILE_PATH).mkdir();
            LoggerUtils.log("已成功创建基础配置文件夹");
        }

        SERVER_MODE = getConfig().getString("server-mode");

        COIServerMode serverMode = COIServerMode.parseCode(SERVER_MODE);

        if(serverMode == null){
            LoggerUtils.log("服务器模式识别失败！");
            this.getServer().shutdown();
        }

        LoggerUtils.log("当前插件模式："+serverMode.getName());

        saveDefaultConfig();

        //注册监听器
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerInteractListener(), this);

        //注册命令类
        getCommand("structure").setExecutor(new COIStructureCommand());

    }

    @Override
    protected void disable() {

    }

    public static Entry getInstance() {
        return instance;
    }

    public static COIBuilder getBuilder() {
        return builder;
    }
}
