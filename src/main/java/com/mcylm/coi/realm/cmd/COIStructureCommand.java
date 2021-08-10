package com.mcylm.coi.realm.cmd;

import com.mcylm.coi.realm.Entry;
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
                LoggerUtils.sendMessage("：这个指令只能让玩家使用。",commandSender);
            }


        }

        return false;
    }
}
