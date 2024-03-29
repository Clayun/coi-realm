package com.mcylm.coi.realm.tools.building.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.model.COIPaster;
import com.mcylm.coi.realm.model.COIStructure;
import com.mcylm.coi.realm.tools.building.Builder;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.utils.FileUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TimeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * COI建筑工具实现
 */
public class COIBuilder implements Builder {

    /**
     * 放建筑文件的文件夹
     */
    private static String STRUCTURE_FOLDER_NAME = "structure/";


    private static final Map<String, COIStructure> cacheMap = new HashMap<>();


    /**
     * 文件后缀
     */
    public static String STRUCTURE_FILE_SUFFIX = "structure";

    /**
     * 粘贴建筑方法，不处理玩家相关逻辑
     * @param paster
     */
    public void pasteStructure(COIPaster paster, @Nullable COIBuilding building){
        pasteStructure(paster,null, building);
    }

    /**
     * 粘贴一个建筑
     * @param paster
     */
    public void pasteStructure(COIPaster paster,Player player, @Nullable COIBuilding building){

        COIStructure structure = paster.getStructure();

        // 全部待建造的方块
        List<COIBlock> allBlocks = structure.getBlocks();

        // 建筑基点
        Location basicLocation = paster.getLocation();

        List<COIBlock> needBuildCOIBlocks = new ArrayList<>();

        List<Block> needBuildBlocks = new ArrayList<>();
        // 根据建筑基点设置每个方块的真实坐标
        for(COIBlock coiBlock : allBlocks){
            coiBlock.setX(coiBlock.getX() + basicLocation.getBlockX());
            coiBlock.setY(coiBlock.getY() + basicLocation.getBlockY());
            coiBlock.setZ(coiBlock.getZ() + basicLocation.getBlockZ());

            if("AIR".equals(coiBlock.getMaterial())
                && !paster.isWithAir()){
                //删除掉空气方块
            } else {
                needBuildCOIBlocks.add(coiBlock);
            }
        }

        // NPC出生点方块名称
        String spawnerBlockTypeName = Entry.getInstance().getConfig().getString("game.npc.spawner-material");

        //根据Y轴排序
        needBuildCOIBlocks.sort(Comparator.comparingDouble(COIBlock::getY));
        needBuildCOIBlocks.forEach(coiBlock -> {

            Block block = Bukkit.getWorld(paster.getWorldName()).getBlockAt(coiBlock.getX(), coiBlock.getY(), coiBlock.getZ());
            if (building != null) {
                block.setMetadata("building", new BuildData(building));
            }
            needBuildBlocks.add(block);

        });

        new BukkitRunnable() {

            // 建造到第几个方块
            int index = 0;

            // NPC出生方块
            private Location spawnLocation = null;
            // 炮口方块
            private Location muzzleLocation = null;

            @Override
            public void run() {

                // 每次建造几个方块
                for(int i = 0; i < paster.getUnit(); i ++){

                    // 如果方块游标还没达到总方块数量，就继续建造
                    if(index <= (needBuildCOIBlocks.size() - 1)){

                        COIBlock coiBlock = needBuildCOIBlocks.get(index);

                        Block block = needBuildBlocks.get(index);

                        // 主线程同步更新世界方块
                        new BukkitRunnable(){

                            @Override
                            public void run() {
                                // 根据COI结构方块获取MC里面的方块
                                Material material = Material.getMaterial(coiBlock.getMaterial());

                                BlockData blockData = Bukkit.createBlockData(coiBlock.getBlockData());

                                if(paster.getBlockColor() != null){
                                    if(isTerracotta(material)){
                                        material = paster.getBlockColor();
                                        blockData = Bukkit.createBlockData(material);
                                    }
                                }

                                if (paster.getCondition().check(block, coiBlock, material)) {
                                    block.setType(paster.getHandler().handle(block, coiBlock, material));
                                }

                                if(block.getType().equals(Material.getMaterial(spawnerBlockTypeName))){
                                    // 如果匹配出生点方块
                                    Location cloneLocation = block.getLocation().clone();
                                    cloneLocation.setY(cloneLocation.getY() + 1);
                                    spawnLocation = cloneLocation;
                                }

                                BlockState state = block.getState();
                                state.setBlockData(blockData);
                                state.update(true);

                                // 设置建造特效
                                block.getWorld().playEffect(block.getLocation(),Effect.STEP_SOUND,1);
                                // 设置玩家提示信息
                                if(player != null){
                                    LoggerUtils.sendActionbar(player,getBuildingProgress(structure.getName(),needBuildCOIBlocks.size(),index,paster.getInterval(),paster.getUnit()));
                                }
                            }

                        }.runTask(Entry.getInstance());

                        ++index;

                    }else{
                        if(player != null){
                            LoggerUtils.sendActionbar(player,getBuildingProgress(structure.getName(),needBuildCOIBlocks.size(),index,paster.getInterval(),paster.getUnit()));
                        }

                        // 建造完成，设置NPC投入生产
                        if(spawnLocation != null){
                            // 赋值出生点
                            paster.setSpawnLocation(spawnLocation);
                            paster.getNpcCreators().forEach(npc -> npc.setSpawnLocation(spawnLocation));
                        }

                        // 通知外部建造完成
                        paster.setComplete(true);

                        this.cancel();
                    }
                }



            }
        }.runTaskTimerAsynchronously(Entry.getInstance(),0,paster.getInterval());

    }

    @Override
    public void pasteStructureWithoutBuilding(COIPaster paster, Player player) {

        COIStructure structure = paster.getStructure();

        // 全部待建造的方块
        List<COIBlock> allBlocks = structure.getBlocks();

        // 建筑基点
        Location basicLocation = paster.getLocation();

        List<COIBlock> needBuildCOIBlocks = new ArrayList<>();

        List<Block> needBuildBlocks = new ArrayList<>();
        // 根据建筑基点设置每个方块的真实坐标
        for(COIBlock coiBlock : allBlocks){
            coiBlock.setX(coiBlock.getX() + basicLocation.getBlockX());
            coiBlock.setY(coiBlock.getY() + basicLocation.getBlockY());
            coiBlock.setZ(coiBlock.getZ() + basicLocation.getBlockZ());

            if("AIR".equals(coiBlock.getMaterial())
                    && !paster.isWithAir()){
                //删除掉空气方块
            } else {
                needBuildCOIBlocks.add(coiBlock);
            }
        }

        // NPC出生点方块名称
        String spawnerBlockTypeName = Entry.getInstance().getConfig().getString("game.npc.spawner-material");

        //根据Y轴排序
        needBuildCOIBlocks.sort(Comparator.comparingDouble(COIBlock::getY));
        needBuildCOIBlocks.forEach(coiBlock -> {

            Block block = Bukkit.getWorld(paster.getWorldName()).getBlockAt(coiBlock.getX(), coiBlock.getY(), coiBlock.getZ());

            needBuildBlocks.add(block);

        });

        new BukkitRunnable() {

            // 建造到第几个方块
            int index = 0;

            // NPC出生方块
            private Location spawnLocation = null;
            // 炮口方块
            private Location muzzleLocation = null;

            @Override
            public void run() {

                // 每次建造几个方块
                for(int i = 0; i < paster.getUnit(); i ++){

                    // 如果方块游标还没达到总方块数量，就继续建造
                    if(index <= (needBuildCOIBlocks.size() - 1)){

                        COIBlock coiBlock = needBuildCOIBlocks.get(index);

                        Block block = needBuildBlocks.get(index);

                        // 主线程同步更新世界方块
                        new BukkitRunnable(){

                            @Override
                            public void run() {
                                // 根据COI结构方块获取MC里面的方块
                                Material material = Material.getMaterial(coiBlock.getMaterial());

                                BlockData blockData = Bukkit.createBlockData(coiBlock.getBlockData());

                                if(paster.getBlockColor() != null){
                                    if(isTerracotta(material)){
                                        material = paster.getBlockColor();
                                        blockData = Bukkit.createBlockData(material);
                                    }
                                }

                                if (paster.getCondition().check(block, coiBlock, material)) {
                                    block.setType(paster.getHandler().handle(block, coiBlock, material));
                                }

                                if(block.getType().equals(Material.getMaterial(spawnerBlockTypeName))){
                                    // 如果匹配出生点方块
                                    Location cloneLocation = block.getLocation().clone();
                                    cloneLocation.setY(cloneLocation.getY() + 1);
                                    spawnLocation = cloneLocation;
                                }

                                BlockState state = block.getState();
                                state.setBlockData(blockData);
                                state.update(true);

                                // 设置建造特效
                                block.getWorld().playEffect(block.getLocation(),Effect.STEP_SOUND,1);
                                // 设置玩家提示信息
                                if(player != null){
                                    LoggerUtils.sendActionbar(player,getBuildingProgress(structure.getName(),needBuildCOIBlocks.size(),index,paster.getInterval(),paster.getUnit()));
                                }
                            }

                        }.runTask(Entry.getInstance());

                        ++index;

                    }else{
                        if(player != null){
                            LoggerUtils.sendActionbar(player,getBuildingProgress(structure.getName(),needBuildCOIBlocks.size(),index,paster.getInterval(),paster.getUnit()));
                        }

                        // 建造完成，设置NPC投入生产
                        if(spawnLocation != null){
                            // 赋值出生点
                            paster.setSpawnLocation(spawnLocation);
                            paster.getNpcCreators().forEach(npc -> npc.setSpawnLocation(spawnLocation));
                        }

                        // 通知外部建造完成
                        paster.setComplete(true);

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

        if(100d == percentbd.doubleValue()){
            message = "&6"+buildingName+"&a 已建造完成";
        }

        return message;
    }


    public COIStructure readStructureFromFile(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(file, COIStructure.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public COIStructure getStructureByFile(String fileName) {

        if (cacheMap.containsKey(fileName)) {
            return cacheMap.get(fileName).clone();
        }

        File file = new File(Entry.PLUGIN_FILE_PATH + STRUCTURE_FOLDER_NAME + fileName);

        if (!file.exists()) {
            return null;
        }

        COIStructure coiStructure = readStructureFromFile(file);

        if (coiStructure != null) {
            cacheMap.put(fileName, coiStructure);
        }

        return coiStructure.clone();

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
            LoggerUtils.log("建筑文件已存在，已直接覆盖");
            // return false;
        }

        boolean b = FileUtils.saveFile(jsonContent, filePath);

        if(b){
            cacheMap.put(structure.getFileName(), structure);
            LoggerUtils.log("建筑文件保存成功");
            return true;
        }

        return false;
    }

    /**
     * 通过两点获取COI结构体
     * @param firstPoint
     * @param secondPoint
     * @return
     */
    public COIStructure getStructureByTwoLocations(Location firstPoint,Location secondPoint){

        double maxX = Math.max(firstPoint.getX(), secondPoint.getX());
        double maxY = Math.max(firstPoint.getY(), secondPoint.getY());
        double maxZ = Math.max(firstPoint.getZ(), secondPoint.getZ());

        double minX = Math.min(firstPoint.getX(), secondPoint.getX());
        double minY = Math.min(firstPoint.getY(), secondPoint.getY());
        double minZ = Math.min(firstPoint.getZ(), secondPoint.getZ());

        firstPoint.set(minX, minY, minZ);
        secondPoint.set(maxX, maxY, maxZ);

        int length = Math.abs(firstPoint.getBlockX() - secondPoint.getBlockX())+1;
        int height = Math.abs(firstPoint.getBlockY() - secondPoint.getBlockY())+1;
        int width = Math.abs(firstPoint.getBlockZ() - secondPoint.getBlockZ())+1;

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

                    int blockX = firstPoint.getBlockX() + x;
                    int blockY = firstPoint.getBlockY() + y;
                    int blockZ = firstPoint.getBlockZ() + z;

                    if(secondPoint.getBlockX() < firstPoint.getBlockX()){
//                        LoggerUtils.debug("P2 X < P1");
                        blockX = firstPoint.getBlockX() + (-1) * x;
                    }

                    if(secondPoint.getBlockY() < firstPoint.getBlockY()){
//                        LoggerUtils.debug("P2 Y < P1");
                        blockY = firstPoint.getBlockY() + (-1) * y;
                    }

                    if(secondPoint.getBlockZ() < firstPoint.getBlockZ()){
//                        LoggerUtils.debug("P2 Z < P1");
                        blockZ = firstPoint.getBlockZ() + (-1) * z;
                    }

                    Block block = firstPoint.getWorld().getBlockAt(blockX,blockY,blockZ);

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

            return coiStructure;
        }

        return null;
    }

    private boolean isTerracotta(Material blockType){
        return blockType.equals(Material.RED_TERRACOTTA)
                || blockType.equals(Material.YELLOW_TERRACOTTA)
                || blockType.equals(Material.GREEN_TERRACOTTA)
                || blockType.equals(Material.BLUE_TERRACOTTA)
                || blockType.equals(Material.BLACK_TERRACOTTA)
                || blockType.equals(Material.PURPLE_TERRACOTTA)
                || blockType.equals(Material.WHITE_TERRACOTTA)
                || blockType.equals(Material.ORANGE_TERRACOTTA)
                || blockType.equals(Material.MAGENTA_TERRACOTTA)
                || blockType.equals(Material.LIGHT_BLUE_TERRACOTTA)
                || blockType.equals(Material.LIME_TERRACOTTA)
                || blockType.equals(Material.PINK_TERRACOTTA)
                || blockType.equals(Material.GRAY_TERRACOTTA)
                || blockType.equals(Material.LIGHT_GRAY_TERRACOTTA)
                || blockType.equals(Material.CYAN_TERRACOTTA)
                || blockType.equals(Material.BROWN_TERRACOTTA);
    }

    public static void placeBlockForBuilding(Block block, COIBuilding building, Material material) {
        building.getBlocks().add(block);
        building.getOriginalBlockData().put(block.getLocation(), block.getBlockData().clone());
        building.getOriginalBlocks().put(block.getLocation(), block.getType());
        block.setType(material);
        block.setMetadata("building", new BuildData(building));
    }
}
