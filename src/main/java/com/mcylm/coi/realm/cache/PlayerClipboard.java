package com.mcylm.coi.realm.cache;

import com.mcylm.coi.realm.tools.COIBuilder;
import com.mcylm.coi.realm.tools.COIStructure;
import com.mcylm.coi.realm.utils.LoggerUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerClipboard {

    private static Map<String,ClipboardLocation> clipboard = new HashMap<>();

    /**
     * 自动选点
     * @param player
     * @param point
     */
    public static void point(Player player,Location point){

        if(player == null
                || !player.isOnline()
                || point == null){
            return;
        }

        boolean b = setFirstPoint(player, point);

        if(!b){
            boolean setSecondPoint = setSecondPoint(player, point);

            if(setSecondPoint){
                int totalBlocks = getCOIStructureByClipboard(player).getBlocks().size();
                LoggerUtils.sendMessage("选取第二个点成功  (X="+point.getBlockX()+",Y="+point.getBlockY()+",Z="+point.getBlockZ()+") ，共计 "+ totalBlocks +" 个方块",player);
            }

        } else
            LoggerUtils.sendMessage("选取第一个点成功  (X="+point.getBlockX()+",Y="+point.getBlockY()+",Z="+point.getBlockZ()+")",player);

    }

    /**
     * 设置第一个点
     * @param player
     * @param point
     */
    private static boolean setFirstPoint(Player player, Location point){

        ClipboardLocation clipboardLocation = clipboard.get(player.getName());

        if(clipboardLocation == null){
            clipboardLocation = new ClipboardLocation();
            clipboardLocation.setFirstPoint(point);
            clipboardLocation.setSecondPoint(null);
            clipboardLocation.setStructure(null);
            clipboard.put(player.getName(),clipboardLocation);
            LoggerUtils.debug("首次选点");
            return true;
        }else{
           //已存在了
            if(clipboardLocation.getFirstPoint() != null
                && clipboardLocation.getSecondPoint() == null){
                //如果第一个点存在，第二个点不存在
                LoggerUtils.debug("如果第一个点存在，第二个点不存在");
                return false;
            }

            if(clipboardLocation.getFirstPoint() != null
                && clipboardLocation.getSecondPoint() != null){
                //如果两个点都存在了，就重置第一个点
                LoggerUtils.debug("如果两个点都存在了，就重置第一个点");
                clipboardLocation.setFirstPoint(point);
                clipboardLocation.setSecondPoint(null);
                clipboardLocation.setStructure(null);
                clipboard.put(player.getName(),clipboardLocation);
                return true;
            }
        }

        //如果上述情况都不符合，就重置
        clipboardLocation.setFirstPoint(point);
        clipboardLocation.setSecondPoint(null);
        clipboardLocation.setStructure(null);
        clipboard.put(player.getName(),clipboardLocation);

        return true;
    }

    /**
     * 设置第二个点
     * @param player
     * @param point
     */
    private static boolean setSecondPoint(Player player, Location point){

        if(clipboard.get(player.getName()) == null){
            return false;
        }

        ClipboardLocation clipboardLocation = clipboard.get(player.getName());
        clipboardLocation.setSecondPoint(point);

        //计算出COI结构体
        COIStructure structure = COIBuilder.getStructureByTwoLocations(clipboardLocation.getFirstPoint(), clipboardLocation.getSecondPoint());
        clipboardLocation.setStructure(structure);

        //将粘贴板的内容存入缓存
        clipboard.put(player.getName(),clipboardLocation);

        return true;
    }

    /**
     * 获取缓存当中的COI结构体
     * @param player
     * @return
     */
    public static COIStructure getCOIStructureByClipboard(Player player){

        ClipboardLocation clipboardLocation = clipboard.get(player.getName());

        if(clipboardLocation == null){
            LoggerUtils.sendMessage("粘贴板为空",player);

            return null;
        }

        return clipboardLocation.getStructure();

    }

    /**
     * 保存粘贴板到文件
     * @param player
     * @param fileName
     * @return
     */
    public static boolean saveStructureFile(Player player,String fileName){

        //获取粘贴板中的文件
        COIStructure structure = getCOIStructureByClipboard(player);

        if(structure == null){
            return false;
        }

        //设置文件名称
        structure.setFileName(fileName + "." + COIBuilder.STRUCTURE_FILE_SUFFIX);
        structure.setName(fileName);

        boolean b = COIBuilder.saveStructureFile(structure);

        if(b){
            LoggerUtils.sendMessage("文件已保存成功",player);
            return true;
        }

        return false;
    }



}
