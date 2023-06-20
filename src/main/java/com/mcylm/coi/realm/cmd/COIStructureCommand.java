package com.mcylm.coi.realm.cmd;

import com.mcylm.coi.realm.clipboard.PlayerClipboard;
import com.mcylm.coi.realm.model.COISkin;
import com.mcylm.coi.realm.utils.LoggerUtils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class COIStructureCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(commandSender instanceof Player player){
            if(player.isOp()){
                // 必须是OP

                if(strings.length < 2){
                    // 请求参数长度错误
                    // incorrect params length
                    LoggerUtils.sendMessage("请求参数长度错误",commandSender);
                    return false;
                }

                // 保存建筑文件
                // save building into a file
                if("save".equals(strings[0])){

                    String fileName = strings[1];

                    // 调用粘贴板中的保存方法
                    // use save method to save file
                    return PlayerClipboard.saveStructureFile(player, fileName);

                }else if("saveSkin".equals(strings[0])){
                    // structure saveSkin <fileName> <name> <buildingTypeCode> <npcSkin>
                    if(strings.length < 4){
                        LoggerUtils.sendMessage("&c请求参数长度错误，/structure saveSkin <fileName> <name> <buildingTypeCode> <npcSkin>",commandSender);
                        return false;
                    }

                    // TODO 实现皮肤保存

                    String fileName = strings[1];

                    // 调用粘贴板中的保存方法
                    // use save method to save file
                    boolean b = PlayerClipboard.saveStructureFile(player, fileName);

                    if(b){
                        // 存储一个皮肤文件到配置文件
                        COISkin coiSkin = new COISkin();
                    }

                    return b;
                }

            }else {
                LoggerUtils.sendMessage("&c没有权限",commandSender);
                return false;
            }
        }else{
            // 这个指令只能让玩家使用
            // This command only player can use
            LoggerUtils.sendMessage("这个指令只能让玩家使用。",commandSender);
            return false;
        }

        return false;
    }
}
