package com.mcylm.coi.realm.tools;

import com.google.gson.Gson;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.utils.FileUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * COI建筑工具类
 */
public class COIBuilder {

    /**
     * 放建筑文件的文件夹
     */
    private static String STRUCTURE_FOLDER_NAME = "structure/";

    /**
     * 创建一个建筑
     * @param structure
     */
    public static void pasteStructure(COIStructure structure){

    }

    /**
     * 创建生成一个建筑文件
     */
    public static boolean saveStructureFile(COIStructure structure){

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
            //如果文件不存在，就创建
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
    public static COIStructure getStructureByTwoLocations(Location point1,Location point2){

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

        //获取两点间的全部坐标点
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
//                    Material material = Material.DIAMOND_BLOCK;
//                    block.setType(material);
//                    block.setBlockData(Bukkit.createBlockData("minecraft:diamond_block"));
//                    block.getState().update(true);

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
            coiStructure.setFileName("newTest.structure");
            coiStructure.setLength(length);
            coiStructure.setHeight(height);
            coiStructure.setWidth(width);
            coiStructure.setName("newTest");

            return coiStructure;
        }

        return null;
    }

}
