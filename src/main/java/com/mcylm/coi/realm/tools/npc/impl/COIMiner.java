package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.runnable.TaskRunnable;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import net.citizensnpcs.api.npc.BlockBreaker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

/**
 * 矿工
 * 相较于普通AI增加了拆方块的功能，可以在此基础上细分工种
 * 矿工会寻找矿物，挖到之后会捡回来放到箱子里，循环反复
 */
public class COIMiner extends COIHuman{

    // 是否正在挖掘中
    private boolean isBreaking = false;

    // 待拆除的方块
    private HashSet<Block> targetBlocks;

    public COIMiner(COIMinerCreator npcCreator) {
        super(npcCreator);
        // 初始化NPC待拆方块
        this.targetBlocks = new HashSet<>();
    }

    /**
     * 工人会清空背包内容
     * @param coiNpc
     * @param respawn 是否重新生成，重新生成会清空背包
     * @return
     */
    public COIHuman update(COIMinerCreator coiNpc, boolean respawn) {

        COIHuman update = super.update(coiNpc, respawn);

        if(update != null && respawn){
            // 初始化NPC待拆方块
            this.targetBlocks = new HashSet<>();
        }

        return update;
    }

    /**
     * 添加需要拆除的方块到 NPC 的缓冲区
     * @return
     */
    private void addBlockTargets() {

        LoggerUtils.debug("添加需要拆除的方块进入缓冲器");

        List<Block> locations = new ArrayList<>();

        // NPC的目标方块类型
        Set<String> blocks =  getCoiNpc().getBreakBlockMaterials();

        // 检测周围需要 7 格半径范围内的全部方块
        List<Block> blocksNearByNpc = getNearbyBlocks(7);

        int i = 0;

        if(blocks != null){
            for(String blockName : blocks){
                for(Block block : blocksNearByNpc){

                    // 比对获取到的附近的方块是否是需要拆除的
                    Material material = Material.getMaterial(blockName);
                    if(material != null){
                        if(material == block.getBlockData().getMaterial()){
                            // 加入到队列当中
                            locations.add(block);
                            i++;
                        }
                    }
                }

            }
        }

        // 添加到缓冲区
        this.targetBlocks.addAll(locations);

        LoggerUtils.debug("添加了："+i+"个方块");

    }

    /**
     * 自动前往需要拆除的方块的位置
     */
    public void findAndBreakBlock(){

        if(!isAlive() || isTooHungryToWork()){
            return;
        }

        // 检查是否还需要继续收集资源
        boolean b = needBackToSaveResources();

        if(b){
            //满足回城条件，回城存放资源
            backAndSaveResources();
            return;
        }

        //优先拆方块
        HashSet<Block> targetBlocks = this.targetBlocks;

        Block targetBlock = null;

        if(targetBlocks != null && targetBlocks.size() > 0){

            LoggerUtils.debug("矿物还有："+targetBlocks.size());

            targetBlock = getNearestBlock(getNpc().getEntity().getLocation());

            if(targetBlock != null){
                if (getNpc().getEntity().getLocation().distance(targetBlock.getLocation()) <= 3) {

                    LoggerUtils.debug("位置小于3");

                    LoggerUtils.debug("isBreaking "+isBreaking);

                    if (!isBreaking) {

                        COIBlock restoreBlock = new COIBlock();
                        restoreBlock.setX(targetBlock.getX());
                        restoreBlock.setY(targetBlock.getY());
                        restoreBlock.setZ(targetBlock.getZ());
                        restoreBlock.setMaterial(targetBlock.getType().name());
                        restoreBlock.setBlockData(targetBlock.getBlockData().getAsString());
                        restoreBlock.setWorld(targetBlock.getWorld().getName());

                        LivingEntity entity = (LivingEntity) getNpc().getEntity();
                        BlockBreaker.BlockBreakerConfiguration blockBreakerConfiguration = new BlockBreaker.BlockBreakerConfiguration();
                        blockBreakerConfiguration.radius(5);
                        blockBreakerConfiguration.item(entity.getEquipment().getItemInMainHand());
                        blockBreakerConfiguration.callback(
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {

                                        // 拆除完成
                                        isBreaking = false;

                                        LoggerUtils.debug("拆除完成了！"+isBreaking);

                                        // 方块复活时间
                                        int restoreTimer = Entry.getInstance().getConfig().getInt("game.mineral-restore-timer");

                                        // 重生矿物方块
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {

                                                Material material = Material.getMaterial(restoreBlock.getMaterial());

                                                BlockData blockData = Bukkit.createBlockData(restoreBlock.getBlockData());

                                                Block block = restoreBlock.getBlock();

                                                block.setType(material);

                                                BlockState state = block.getState();
                                                state.setBlockData(blockData);
                                                state.update(true);

                                                this.cancel();

                                            }
                                        }.runTaskLater(Entry.getInstance(),20 * restoreTimer);

                                    }
                                }
                        );
                        BlockBreaker breaker = getNpc().getBlockBreaker(targetBlock, blockBreakerConfiguration);
                        if (breaker.shouldExecute()) {
                            LoggerUtils.debug("开始挖矿");
                            isBreaking = true;
                            TaskRunnable run = new TaskRunnable(breaker);
                            run.setTaskId(Bukkit.getScheduler().scheduleSyncRepeatingTask(Entry.getInstance(), run, 0, 1));
                        }
                    }else{
                        LoggerUtils.debug("还在挖呢");
                    }

                } else {

                    findPath(targetBlock.getLocation());

                    LoggerUtils.debug("寻路中");
                }
            }


        }

        addBlockTargets();
    }

    /**
     * 获取最近的矿物
     * @param entityLocation
     * @return
     */
    private Block getNearestBlock(Location entityLocation){

        double distance = 99999999d;
        Block targetBlock = null;

        Iterator<Block> iterator = targetBlocks.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();

            if(block.getType() == Material.AIR){
                iterator.remove();
            }

            if(block.getLocation().distance(entityLocation) < distance){
                if(canStand(block.getLocation())){
                    targetBlock = block;
                    distance = block.getLocation().distance(entityLocation);
                }
            }
        }

        return targetBlock;
    }

    /**
     * 挖的资源足够多了，需要回去交付
     * @return
     */
    private boolean needBackToSaveResources(){
        List<ItemStack> inventory = getCoiNpc().getInventory();

        if(inventory.isEmpty()){
            return false;
        }

        int count = 0;

        for(ItemStack i : inventory){
            Set<String> picks = getCoiNpc().getPickItemMaterials();
            if(picks != null && picks.size() > 0){

                for(String pickItemName : picks){
                    Material material = Material.getMaterial(pickItemName);
                    if(material != null){
                        if(i.getType() == material) {

                            for(String foodName : Entry.getNpcFoods()){
                                Material food = Material.getMaterial(foodName);

                                if(i.getType() != food){
                                    // 食物不计算在内
                                    count = i.getAmount() + count;
                                }

                            }

                        }
                    }

                }
            }
        }

        COIMinerCreator coiNpc = (COIMinerCreator) getCoiNpc();

        if(count >= coiNpc.getResourceLimitToBack()){
            // 满足回城条件，回去
            return true;
        }

        return false;
    }

    /**
     * 回去并存放资源
     */
    private void backAndSaveResources(){

        COIMinerCreator coiNpc = (COIMinerCreator) getCoiNpc();

        List<Location> chestsLocation = coiNpc.getChestsLocation();

        if(chestsLocation.isEmpty()){
            // 原地待命
            return;
        }

        // 没有满的箱子
        Location notFullChestLocation = getEmptyChestByLocations(chestsLocation);

        if(notFullChestLocation == null){
            // 原地待命
            return;
        }

        findPath(notFullChestLocation);

        if(getLocation() != null){
            if(getLocation().distance(notFullChestLocation) < 3){
                List<ItemStack> inventory = getCoiNpc().getInventory();
                getCoiNpc().setInventory(new ArrayList<>());

                for(ItemStack itemStack : inventory){
                    if(itemStack != null && !itemStack.getType().equals(Material.AIR)){

                        List<String> npcFoods = Entry.getNpcFoods();

                        for(String foodName : npcFoods){

                            Material material = Material.getMaterial(foodName);

                            // 不是食物的就丢进箱子里
                            if(!itemStack.getType().equals(material)){
                                ItemUtils.addItemIntoChest(notFullChestLocation,itemStack);
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * 找一个相对比较空的箱子装东西
     * @param chestsLocation
     * @return
     */
    private Location getEmptyChestByLocations(List<Location> chestsLocation){
        for(Location location : chestsLocation){
            Block block = location.getBlock();
            if(block.getType().equals(Material.CHEST)){
                Chest chest = (Chest) block.getState();

                int i = chest.getInventory().firstEmpty();

                if(i != -1){
                    // 没有满
                    return location;
                }
            }
        }

        return null;
    }



    public void move() {
        super.move();
        //找可拆除的去拆
        findAndBreakBlock();
    }

}
