package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.runnable.TaskRunnable;
import com.mcylm.coi.realm.tools.npc.COICartCreator;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.BlockBreaker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * 矿车
 * 会自动把矿工挖的绿宝石运输到
 */
public class COICart extends COIEntity {

    private boolean needCharging = false;


    public COICart(COICartCreator npcCreator) {
        super(npcCreator);
    }

    /**
     * 清空背包内容
     * @param coiNpc
     * @param respawn 是否重新生成，重新生成会清空背包
     * @return
     */
    public COIEntity update(COIMinerCreator coiNpc, boolean respawn) {

        COIEntity update = super.update(coiNpc, respawn);

        if(update != null && respawn){

        }

        return update;
    }



    /**
     * 行动
     */
    private void action(){

        if(getCoiNpc().getInventory().isEmpty()){
            collectingResources();
        }else{
            transportResources();
        }
    }

    /**
     * 收取资源
     */
    private void collectingResources(){

        LoggerUtils.debug("寻找可以运输的资源");

        COICartCreator coiNpc = (COICartCreator) getCoiNpc();

        List<Location> chestsLocation = coiNpc.getChestsLocation();

        if(chestsLocation.isEmpty()){
            // 返回出生点
            findPath(coiNpc.getSpawnLocation());
            return;
        }

        // 没有满的箱子
        Location notEmptyChestLocation = getNotEmptyChestByLocations(chestsLocation);

        if(notEmptyChestLocation == null){
            // 返回出生点
            findPath(coiNpc.getSpawnLocation());
            return;
        }

        findPath(notEmptyChestLocation);

        if(getLocation() != null){
            if(getLocation().distance(notEmptyChestLocation) < 3){

                Block block = notEmptyChestLocation.getBlock();
                if(ItemUtils.SUITABLE_CONTAINER_TYPES.contains(block.getType())){
                    Container chest = (Container) block.getState();

                    for(ItemStack itemStack : chest.getInventory()){

                        if(itemStack != null && !itemStack.getType().equals(Material.AIR)){

                            Map<Integer, ItemStack> extra = coiNpc.getInventory().addItem(itemStack);
                            if (extra.isEmpty()) {
                                chest.getInventory().remove(itemStack);
                            } else {
                                extra.values().forEach(i -> itemStack.setAmount(i.getAmount()));
                            }

                        }
                    }
                }


            }
        }
    }

    /**
     * 运输资源
     */
    private void transportResources(){

        LoggerUtils.debug("开始配送资源资源");

        COICartCreator coiNpc = (COICartCreator) getCoiNpc();

        List<Location> chestsLocation = coiNpc.getToSaveResourcesLocations();

        if(chestsLocation.isEmpty()){
            LoggerUtils.debug("存放资源的箱子不存在");
            // 返回出生点
            findPath(coiNpc.getSpawnLocation());
            return;
        }

        // 没有满的箱子
        Location notFullChestLocation = getEmptyChestByLocations(chestsLocation);

        if(notFullChestLocation == null){
            LoggerUtils.debug("存放资源的箱子都满了");
            // 返回出生点
            findPath(coiNpc.getSpawnLocation());
            return;
        }

        findPath(notFullChestLocation);

        if(getLocation() != null){
            if(getLocation().distance(notFullChestLocation) < 3){

                for(ItemStack itemStack : coiNpc.getInventory()){
                    if(itemStack != null && !itemStack.getType().equals(Material.AIR)){

                        List<String> npcFoods = Entry.getNpcFoods();


                        // 不是食物的就丢进箱子里
                        if(!npcFoods.contains(itemStack.getType().toString())){

                            Map<Integer, ItemStack> extra = ItemUtils.addItemIntoContainer(notFullChestLocation, itemStack);
                            if (extra.isEmpty()) {
                                coiNpc.getInventory().remove(itemStack);
                            } else {
                                extra.values().forEach(i -> itemStack.setAmount(i.getAmount()));
                            }

                        }
                    }
                }
            }
        }
    }
    @Override
    public void findPath(Location location) {
        if (!isAlive()) {
            return;
        }

        if (!npc.isSpawned()) {
            return;
        }

        npc.faceLocation(location);

        Navigator navigator = npc.getNavigator();
        navigator.getDefaultParameters()
                .stuckAction(null)
                .useNewPathfinder(false);

        navigator.setTarget(location);

    }



    /**
     * 找一个相对比较空的箱子装东西
     * @param chestsLocation
     */
    private Location getEmptyChestByLocations(List<Location> chestsLocation){
        for(Location location : chestsLocation){
            Block block = location.getBlock();
            if(ItemUtils.SUITABLE_CONTAINER_TYPES.contains(block.getType())){
                Container chest = (Container) block.getState();

                int i = chest.getInventory().firstEmpty();

                if(i != -1){
                    // 没有满
                    return location;
                }
            }
        }

        return null;
    }

    /**
     * 找一个有物资的箱子
     * @param chestsLocation
     */
    private Location getNotEmptyChestByLocations(List<Location> chestsLocation){
        for(Location location : chestsLocation){
            Block block = location.getBlock();
            if(ItemUtils.SUITABLE_CONTAINER_TYPES.contains(block.getType())){
                Container chest = (Container) block.getState();

                if(!chest.getInventory().isEmpty()){
                    return location;
                }
            }
        }

        return null;
    }

    /**
     * 自动充电
     * @return 是否有足够的电量
     */
    private boolean automaticCharging(){
        if(getHunger() <= 7){
            say("电池电量过低，准备返回充电桩");
            findPath(getCoiNpc().getSpawnLocation());
            needCharging = true;
            return false;
        }

        if(getLocation().distance(getCoiNpc().getSpawnLocation()) <= 3){
            // 开始充电
            say("充电中...");

            // 最大电量 20
            if(getHunger() < 20){
                setHunger(getHunger() + 0.5);

                // 如果开启强制充电，就充满
                if(needCharging){
                    if(getHunger() >= 20){
                        needCharging = false;
                        return true;
                    }
                }
            }


        }

        // 未开启强制充电，就呆几秒，充几秒
        return true;
    }

    @Override
    public void move() {
        super.move();
        // 电池电量检测
        boolean b = automaticCharging();

        if(b){
            // 开始行动
            action();
        }

    }

}
