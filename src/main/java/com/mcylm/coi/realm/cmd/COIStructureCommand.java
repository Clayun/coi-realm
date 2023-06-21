package com.mcylm.coi.realm.cmd;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.clipboard.PlayerClipboard;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIPermissionType;
import com.mcylm.coi.realm.model.COISkin;
import com.mcylm.coi.realm.tools.data.SkinData;
import com.mcylm.coi.realm.utils.LoggerUtils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    if(strings.length < 7){
                        LoggerUtils.sendMessage("&c请求参数长度错误，/structure saveSkin <fileName> <name> <buildingTypeCode> <npcSkin> <level> <material>",commandSender);
                        return false;
                    }

                    // 皮肤保存

                    String fileName = strings[1];
                    String name = strings[2];
                    String buildingTypeCode = strings[3];
                    String npcSkin = strings[4];
                    Integer level = Integer.valueOf(strings[5]);
                    String material = strings[6];

                    // 调用粘贴板中的保存方法
                    // use save method to save file
                    boolean b = PlayerClipboard.saveStructureFile(player, fileName);

                    if(b){
                        // 存储一个皮肤文件到配置文件

                        // 先检测皮肤code是否已存在
                        List<COISkin> skins = Entry.getSkinData().getSkins();
                        boolean matched = false;

                        for(COISkin skin : skins){
                            if(skin.getCode().equals(fileName)){
                                // 如果存在，就在map里面添加新的等级，如果等级重复，就覆盖掉
                                skin.setName(name);
                                skin.setMaterial(material);
                                skin.setNpcSkin(npcSkin);
                                skin.getBuildingLevelStructure().put(level,fileName);
                                matched = true;
                            }
                        }

                        if(matched){
                            // 存在的情况下，就重新保存一下文件
                            SkinData skinData = new SkinData();
                            skinData.setSkins(skins);
                            Entry.getInstance().saveSkinData(skinData);
                        }else{
                            // 没有匹配到，创建一个新的皮肤
                            COIBuildingType buildingTypeByCode = COIBuildingType.getBuildingTypeByCode(buildingTypeCode);
                            if(buildingTypeByCode == null){
                                LoggerUtils.sendMessage("&c建筑类型不存在",commandSender);
                                return false;
                            }

                            // 默认初始化5个等级
                            Map<Integer, String> buildingLevelStructure = new HashMap<>();
                            buildingLevelStructure.put(1,fileName + ".structure");
                            buildingLevelStructure.put(2,fileName + ".structure");
                            buildingLevelStructure.put(3,fileName + ".structure");
                            buildingLevelStructure.put(4,fileName + ".structure");
                            buildingLevelStructure.put(5,fileName + ".structure");

                            COISkin coiSkin = new COISkin(fileName,name,material, COIPermissionType.SKIN_PERMISSION.getCode() + fileName,buildingTypeByCode.getCode(),buildingLevelStructure,npcSkin);
                            Entry.getSkinData().getSkins().add(coiSkin);
                            Entry.getInstance().saveSkinData();
                        }

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
