package com.mcylm.coi.realm.tools;

import com.google.gson.Gson;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.utils.FileUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * COI建筑工具类
 */
public class COIBuilder {

    /**
     * 放建筑文件的文件夹
     */
    private static String STRUCTURE_FOLDER_NAME = "structure";

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

        String filePath = "plugins/" + Entry.getInstance().getName() + "/" + STRUCTURE_FOLDER_NAME + "/" + structure.getFileName();

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

}
