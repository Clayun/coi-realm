package com.mcylm.coi.realm.tools;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.api.Builder;
import com.mcylm.coi.realm.utils.FileUtils;
import com.mcylm.coi.realm.utils.JsonUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import me.lucko.helper.Schedulers;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * COI建筑工具实现
 */
public class COIBuilder implements Builder {

    /**
     * 放建筑文件的文件夹
     */
    private static String STRUCTURE_FOLDER_NAME = "structure/";

    /**
     * 文件后缀
     */
    public static String STRUCTURE_FILE_SUFFIX = "structure";

    /**
     * 粘贴建筑方法，不处理玩家相关逻辑
     * @param paster
     */
    public void pasteStructure(COIPaster paster){
        pasteStructure(paster,null);
    }

    /**
     * 粘贴一个建筑
     * @param paster
     */
    public void pasteStructure(COIPaster paster,Player player){

        COIStructure structure = paster.getStructure();

        // 全部待建造的方块
        List<COIBlock> allBlocks = structure.getBlocks();

        // 建筑基点
        Location basicLocation = paster.getLocation();

        List<COIBlock> needBuildBlocks = new ArrayList<>();

        // 根据建筑基点设置每个方块的真实坐标
        for(COIBlock coiBlock : allBlocks){
            coiBlock.setX(coiBlock.getX() + basicLocation.getBlockX());
            coiBlock.setY(coiBlock.getY() + basicLocation.getBlockY());
            coiBlock.setZ(coiBlock.getZ() + basicLocation.getBlockZ());

            if("AIR".equals(coiBlock.getMaterial())
                && !paster.isWithAir()){
                //删除掉空气方块
            }else
                needBuildBlocks.add(coiBlock);


        }

        //根据Y轴排序
        Collections.sort(needBuildBlocks, Comparator.comparingDouble(COIBlock::getY));

        new BukkitRunnable() {

            // 建造到第几个方块
            int index = 0;

            @Override
            public void run() {

                // 每次建造几个方块
                for(int i = 0; i < paster.getUnit(); i ++){

                    // 如果方块游标还没达到总方块数量，就继续建造
                    if(index <= (needBuildBlocks.size() - 1)){

                        COIBlock coiBlock = needBuildBlocks.get(index);

                        // 主线程同步更新世界方块
                        new BukkitRunnable(){

                            @Override
                            public void run() {
                                // 根据COI结构方块获取MC里面的方块
                                Block block = Bukkit.getWorld(paster.getWorldName()).getBlockAt(coiBlock.getX(),coiBlock.getY(),coiBlock.getZ());
                                Material material = Material.getMaterial(coiBlock.getMaterial());
                                block.setType(material);
                                block.setBlockData(Bukkit.createBlockData(coiBlock.getBlockData()));
                                block.getState().update(true);

                                // 设置建造特效
                                block.getWorld().playEffect(block.getLocation(),Effect.STEP_SOUND,1);
                                // 设置玩家提示信息
                                if(player != null){
                                    LoggerUtils.sendActionbar(player,getBuildingProgress(structure.getName(),needBuildBlocks.size(),index,paster.getInterval(),paster.getUnit()));
                                }
                            }

                        }.runTask(Entry.getInstance());

                        ++index;

                    }else{
                        this.cancel();
                    }
                }



            }
        }.runTaskTimerAsynchronously(Entry.getInstance(),0,paster.getInterval());

    }

    /**
     * 获取建造进度 Actionbar 内容
     * @param buildingName 建筑名称
     * @param totalBlocks 总方块数量
     * @param index 进度游标
     * @param interval 多长时间间隔一次，单位 tick
     * @param unit 每次建造多少个方块
     * @return
     */
    private String getBuildingProgress(String buildingName,int totalBlocks,int index,Long interval,int unit){

        // 进度百分数
        float percent = ((totalBlocks - (float)((totalBlocks - index))) / totalBlocks) * 100;

        // 建造速度
        float v = (float)20/(float)interval*(float)unit;

        // 剩余建造时间
        float time = ((float)totalBlocks - (float)index) / v;

        //保留两位小数
        BigDecimal bd = new BigDecimal(time).setScale(0, BigDecimal.ROUND_HALF_UP);
        BigDecimal percentbd = new BigDecimal(percent).setScale(2, BigDecimal.ROUND_HALF_UP);

        String message = "&6"+buildingName+"&a 建造中 建造进度：&6"+percentbd+"% &a剩余时间：&6"+ TimeUtils.formatDateTime(bd.longValue());

        return message;
    }

    /**
     * 读取文件获取COI建筑结构体
     * @param fileName
     * @return
     */
    public COIStructure getStructureByFile(String fileName){

        File file = new File(Entry.PLUGIN_FILE_PATH + STRUCTURE_FOLDER_NAME + fileName);

        if(!file.exists()){
            return null;
        }

        String s = JsonUtils.readJsonFile(Entry.PLUGIN_FILE_PATH + STRUCTURE_FOLDER_NAME + fileName);

        COIStructure coiStructure = JSONObject.parseObject(s, COIStructure.class);

        return coiStructure;
    }

    /**
     * 创建生成一个建筑文件
     */
    public boolean saveStructureFile(COIStructure structure){

        /**
         * 判断空值
         */
        if(StringUtils.isBlank(structure.getFileName())
            || StringUtils.isBlank(structure.getName())
            || structure.getBlocks() == null
            || structure.getBlocks().isEmpty()){

            LoggerUtils.log("建筑文件名称为空");
            return false;
        }

        Gson gson = new Gson();

        String jsonContent = gson.toJson(structure);

        String filePath = Entry.PLUGIN_FILE_PATH + STRUCTURE_FOLDER_NAME + structure.getFileName();

        File file = new File(filePath);

        if(!file.exists()){
            // 如果文件不存在，就创建
            FileUtils.createFileByPath(filePath);
        }else{
            LoggerUtils.log("建筑文件已存在，请更换名称后重新保存");
            return false;
        }

        boolean b = FileUtils.saveFile(jsonContent, filePath);

        if(b){
            LoggerUtils.log("建筑文件保存成功");
            return true;
        }

        return false;
    }

    /**
     * 通过两点获取COI结构体
     * @param point1
     * @param point2
     * @return
     */
    public COIStructure getStructureByTwoLocations(Location point1,Location point2){

        int length = Math.abs(point1.getBlockX() - point2.getBlockX())+1;
        int height = Math.abs(point1.getBlockY() - point2.getBlockY())+1;
        int width = Math.abs(point1.getBlockZ() - point2.getBlockZ())+1;

        LoggerUtils.debug("建筑长 "+length);
        LoggerUtils.debug("建筑高 "+height);
        LoggerUtils.debug("建筑宽 "+width);

        int coiX = 0;
        int coiY = 0;
        int coiZ = 0;

        COIStructure coiStructure = new COIStructure();

        List<COIBlock> blocks = new ArrayList<>();

        // 获取两点间的全部坐标点
        for(int x = 0; x < length; x++){
            for(int y = 0; y < height; y++){
                for(int z = 0; z < width; z++){

                    coiX = x;
                    coiY = y;
                    coiZ = z;

                    int blockX = point1.getBlockX() + x;
                    int blockY = point1.getBlockY() + y;
                    int blockZ = point1.getBlockZ() + z;

                    if(point2.getBlockX() < point1.getBlockX()){
                        LoggerUtils.debug("P2 X < P1");
                        blockX = point1.getBlockX() + (-1) * x;
                    }

                    if(point2.getBlockY() < point1.getBlockY()){
                        LoggerUtils.debug("P2 Y < P1");
                        blockY = point1.getBlockY() + (-1) * y;
                    }

                    if(point2.getBlockZ() < point1.getBlockZ()){
                        LoggerUtils.debug("P2 Z < P1");
                        blockZ = point1.getBlockZ() + (-1) * z;
                    }

                    Block block = point1.getWorld().getBlockAt(blockX,blockY,blockZ);

                    COIBlock coiBlock = new COIBlock();
                    coiBlock.setX(coiX);
                    coiBlock.setY(coiY);
                    coiBlock.setZ(coiZ);
                    coiBlock.setMaterial(block.getBlockData().getMaterial().name());
                    coiBlock.setBlockData(block.getBlockData().getAsString());

                    blocks.add(coiBlock);

                }
            }
        }

        if(!blocks.isEmpty()){
            coiStructure.setBlocks(blocks);

            coiStructure.setLength(length);
            coiStructure.setHeight(height);
            coiStructure.setWidth(width);
//            coiStructure.setName("newTest");
//            coiStructure.setFileName("newTest.structure");

            return coiStructure;
        }

        return null;
    }

}
