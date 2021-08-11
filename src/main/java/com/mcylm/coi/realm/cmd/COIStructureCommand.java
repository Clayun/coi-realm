package com.mcylm.coi.realm.cmd;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.cache.PlayerClipboard;
import com.mcylm.coi.realm.tools.COIBuilder;
import com.mcylm.coi.realm.tools.COIStructure;
import com.mcylm.coi.realm.utils.LoggerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class COIStructureCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(command.getName().equalsIgnoreCase("structure")){
            if(!(commandSender instanceof Player)){
                LoggerUtils.sendMessage("这个指令只能让玩家使用。",commandSender);
                return false;
            }

            if(strings.length < 2){
                LoggerUtils.sendMessage("请求参数长度错误",commandSender);
                return false;
            }

            Player player = (Player) commandSender;

            //保存建筑文件
            if("save".equals(strings[0])){

                String fileName = strings[1];

                //调用粘贴板中的保存方法
                return PlayerClipboard.saveStructureFile(player, fileName);

            }

        }

        return false;
    }
}
