package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.runnable.TaskRunnable;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import net.citizensnpcs.api.npc.BlockBreaker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
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
    private List<Block> targetBlocks;

    public COIMiner(COIMinerCreator npcCreator) {
        super(npcCreator);
        // 初始化NPC待拆方块
        this.targetBlocks = new ArrayList<>();
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
            this.targetBlocks = new ArrayList<>();
        }

        return update;
    }

    /**
     * 添加需要拆除的方块到 NPC 的缓冲区
     * @return
     */
    private void addBlockTargets() {

        List<Block> locations = new ArrayList<>();

        // NPC的目标方块类型
        Set<String> blocks =  getCoiNpc().getBreakBlockMaterials();

        // 检测周围需要 10 格半径范围内的全部方块
        List<Block> blocksNearByNpc = getNearbyBlocks(10);

        if(blocks != null){
            for(String blockName : blocks){
                for(Block block : blocksNearByNpc){

                    // 比对获取到的附近的方块是否是需要拆除的
                    Material material = Material.getMaterial(blockName);
                    if(material != null){
                        if(material == block.getBlockData().getMaterial()){
                            // 加入到队列当中
                            locations.add(block);
                        }
                    }
                }

            }
        }

        // 添加到缓冲区
        this.targetBlocks.addAll(locations);

        // 根据Y轴排序
        Collections.sort(this.targetBlocks, Comparator.comparingDouble(Block::getY));

        // 再次翻转，优先拆除高的方块，实现的效果是从高往低挖
        Collections.reverse(this.targetBlocks);

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
        List<Block> targetBlocks = this.targetBlocks;

        Block targetBlock = null;

        if(targetBlocks != null && targetBlocks.size() > 0){

            Iterator<Block> iterator = targetBlocks.iterator();
            while (iterator.hasNext()) {
                targetBlock = iterator.next();
                if(targetBlock != null){

                    Location clone = targetBlock.getLocation().clone();
                    clone.setY(clone.getY()+1);

                    if(targetBlock.getWorld().getBlockAt(targetBlock.getLocation()).getType() == Material.AIR){
                        iterator.remove();
                    }else{
                        if (getNpc().getEntity().getLocation().distance(targetBlock.getLocation()) <= 3) {

                            if (!isBreaking) {

                                LivingEntity entity = (LivingEntity) getNpc().getEntity();
                                BlockBreaker.BlockBreakerConfiguration blockBreakerConfiguration = new BlockBreaker.BlockBreakerConfiguration();
                                blockBreakerConfiguration.radius(5);
                                blockBreakerConfiguration.item(entity.getEquipment().getItemInMainHand());
                                blockBreakerConfiguration.callback(
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                //拆除完成
                                                isBreaking = false;
                                            }
                                        }
                                );
                                isBreaking = true;
                                BlockBreaker breaker = getNpc().getBlockBreaker(targetBlock, blockBreakerConfiguration);
                                if (breaker.shouldExecute()) {
                                    TaskRunnable run = new TaskRunnable(breaker);
                                    run.setTaskId(Bukkit.getScheduler().scheduleSyncRepeatingTask(Entry.getInstance(), run, 0, 1));
                                }
                            }


                        } else {

                            getNpc().faceLocation(targetBlock.getLocation());
                            findPath(targetBlock.getLocation());

                        }
                    }
                }
            }
        }else{
            addBlockTargets();
        }
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
