package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.runnable.TaskRunnable;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import net.citizensnpcs.api.npc.BlockBreaker;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * 农夫
 * 角色会将身上有的小麦都做成面包，并放到箱子里
 * 如果没有小麦，就会寻找附近的泥土，开始种植，并使用骨粉催熟，循环反复
 */
public class COIFarmer extends COIHuman{

    // 耕地
    private List<Block> farmlands;

    // 独立背包
    private List<ItemStack> farmerInventory;

    // 缓存手里的物品
    private ItemStack itemInHand;

    public COIFarmer(COINpc npcCreator) {
        super(npcCreator);
        farmlands = new ArrayList<>();
        farmerInventory = new ArrayList<>();
    }

    /**
     * 将背包里的小麦制作成面包
     */
    private void makeWheatToBread(){

        if(!isAlive()){
            return;
        }

        List<ItemStack> backpack = getFarmerInventory();

        if(!backpack.isEmpty()){

            Iterator<ItemStack> iterator = getFarmerInventory().iterator();
            while (iterator.hasNext()) {
                ItemStack item = iterator.next();
                if (item != null) {
                    if(item.getType().equals(Material.WHEAT)){
                        ItemStack bread = new ItemStack(Material.BREAD);
                        bread.setAmount(item.getAmount());

                        if(getHunger() <= 19){
                            getCoiNpc().getFoodBag().add(bread);
                        }else{
                            getCoiNpc().getInventory().add(bread);
                        }
                        iterator.remove();
                    }
                }
            }

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
            if(i.getType().equals(Material.BREAD)){
                count = i.getAmount() + count;
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
     * 寻找泥土并把泥土变成耕地
     */
    public void findDirtAndMakeItToFarmland(){

        if(!isAlive()){
            return;
        }

        // 检查是否需要回城存放东西
        boolean b = needBackToSaveResources();

        if(b){
            backAndSaveResources();
            return;
        }

        List<Block> nearbyBlocks = getNearbyBlocks(10);

        for(Block block : nearbyBlocks){

            if(block.getType().equals(Material.FARMLAND)){
                farmlands.add(block);
                continue;
            }

            if(block.getType().equals(Material.DIRT)){

                //泥土上方必须是空气或者草才可以
                Location clone = block.getLocation().clone();
                clone.setY(clone.getY()+1);
                if(clone.getBlock().getType().equals(Material.AIR)
                    || clone.getBlock().getType().equals(Material.GRASS)
                    || clone.getBlock().getType().equals(Material.TALL_GRASS)
                ){
                    // 如果找到泥土了，就去把他变成耕地
                    if(getNpc().getEntity().getLocation().distance(block.getLocation()) <= 3){

                        LivingEntity entity = (LivingEntity) getNpc().getEntity();

                        if(entity.getEquipment().getItemInMainHand().getType().equals(Material.BONE_MEAL)){
                            if(itemInHand != null){
                                entity.getEquipment().setItemInMainHand(itemInHand);
                            }
                        }

                        // 挥动手作为动作动画
                        ((LivingEntity)getNpc().getEntity()).swingMainHand();

                        Material material = Material.FARMLAND;
                        block.setType(material);

                        BlockState state = block.getState();
                        state.update(true);

                        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND,1);

                        farmlands.add(block);

                        // 每次只弄一个
                        break;
                    }else{
                        // 距离不够，就跑过去
                        getNpc().faceLocation(block.getLocation());
                        findPath(block.getLocation());
                    }
                }


            }
        }

    }

    /**
     * 回去并存放资源
     */
    public void backAndSaveResources(){

        if(!isAlive()){
            return;
        }

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

                        // 把面包丢进箱子里
                        if(itemStack.getType().equals(Material.BREAD)){
                            ItemUtils.addItemIntoChest(notFullChestLocation,itemStack);
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

    /**
     * 打掉小麦并将其捡起来
     * 种植小麦
     */
    private void plantAndHarvestWheat(){

        if(!isAlive()){
            return;
        }

        // 检查是否需要回城存放东西
        boolean b = needBackToSaveResources();

        if(b){
            // 需要回城，就中断执行下面的方法
            return;
        }

        List<Block> farmlands = getFarmlands();

        // 打乱排序，避免每次都找那一块耕地使劲种
        Collections.shuffle(farmlands);

        if(!farmlands.isEmpty()){
            for(Block block : farmlands){

                // 如果方块还是耕地
                if(block.getType().equals(Material.FARMLAND)){
                    // 获取耕地上面的东西
                    Location clone = block.getLocation().clone();
                    clone.setY(clone.getY()+1);
                    Block wheat = clone.getBlock();
                    if(wheat.getType().equals(Material.WHEAT)){
                        // 如果上面是小麦，已经生长至7级
                        // 同时距离小于3格，就打掉他
                        final Ageable ageable = (Ageable) wheat.getState().getBlockData();
                        if (ageable.getAge() == 7){
                            if(getNpc().getEntity().getLocation().distance(wheat.getLocation()) <= 2){
                                LivingEntity entity = (LivingEntity) getNpc().getEntity();

                                // 面朝方块
                                getNpc().faceLocation(wheat.getLocation());

                                entity.getEquipment().setItemInMainHand(itemInHand);

                                BlockBreaker.BlockBreakerConfiguration blockBreakerConfiguration = new BlockBreaker.BlockBreakerConfiguration();
                                blockBreakerConfiguration.radius(5);
                                blockBreakerConfiguration.item(entity.getEquipment().getItemInMainHand());
                                blockBreakerConfiguration.callback(
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                //拆除完成

                                            }
                                        }
                                );

                                BlockBreaker breaker = getNpc().getBlockBreaker(wheat, blockBreakerConfiguration);
                                if (breaker.shouldExecute()) {
                                    TaskRunnable run = new TaskRunnable(breaker);
                                    run.setTaskId(Bukkit.getScheduler().scheduleSyncRepeatingTask(Entry.getInstance(), run, 0, 1));
                                }
                            }else{
                                // 距离不够，就跑过去
                                getNpc().faceLocation(wheat.getLocation());
                                findPath(wheat.getLocation());
                            }
                        }else{
                            // 催熟，每次长1
                            if(getNpc().getEntity().getLocation().distance(wheat.getLocation()) <= 2){
                                LivingEntity entity = (LivingEntity) getNpc().getEntity();

                                if(itemInHand == null){
                                    itemInHand = entity.getEquipment().getItemInMainHand();
                                }

                                // 面朝方块
                                getNpc().faceLocation(wheat.getLocation());

                                // 挥动手作为动作动画
                                ((LivingEntity)getNpc().getEntity()).swingMainHand();

                                entity.getEquipment().setItemInMainHand(new ItemStack(Material.BONE_MEAL));

                                ageable.setAge(ageable.getAge()+1);
                                wheat.setBlockData(ageable);
                                wheat.getState().update();

                                // 每次只搞一个
                                break;
                            }else{
                                // 距离不够，就跑过去
                                getNpc().faceLocation(wheat.getLocation());
                                findPath(wheat.getLocation());
                            }

                        }

                    }else if(wheat.getType().equals(Material.AIR)){
                        // 如果耕地的上方是空气
                        // 同时距离小于3，就种植小麦
                        if(getNpc().getEntity().getLocation().distance(wheat.getLocation()) <= 2){

                            // 面朝方块
                            getNpc().faceLocation(wheat.getLocation());

                            // 挥动手作为动作动画
                            ((LivingEntity)getNpc().getEntity()).swingMainHand();

                            Material material = Material.WHEAT;
                            wheat.setType(material);

                            BlockState state = wheat.getState();
                            state.update(true);

                            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND,1);

                        }else{
                            // 距离不够，就跑过去
                            getNpc().faceLocation(wheat.getLocation());
                            findPath(wheat.getLocation());
                        }

                    }
                }else if (block.getType().equals(Material.DIRT)){
                    // 如果耕地变成了泥土
                    // 泥土上方必须是空气才可以
                    Location clone = block.getLocation().clone();
                    clone.setY(clone.getY()+1);
                    if(clone.getBlock().getType().equals(Material.AIR)){
                        // 如果找到泥土了，就去把他变成耕地
                        if(getNpc().getEntity().getLocation().distance(block.getLocation()) <= 3){

                            LivingEntity entity = (LivingEntity) getNpc().getEntity();

                            if(entity.getEquipment().getItemInMainHand().getType().equals(Material.BONE_MEAL)){
                                if(itemInHand != null){
                                    entity.getEquipment().setItemInMainHand(itemInHand);
                                }
                            }

                            // 面朝方块
                            getNpc().faceLocation(block.getLocation());

                            // 挥动手作为动作动画
                            ((LivingEntity)getNpc().getEntity()).swingMainHand();

                            Material material = Material.FARMLAND;
                            block.setType(material);

                            BlockState state = block.getState();
                            state.update(true);

                            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND,1);

                            farmlands.add(block);

                            // 每次只弄一个
                            break;
                        }else{
                            // 距离不够，就跑过去
                            getNpc().faceLocation(block.getLocation());
                            findPath(block.getLocation());
                        }
                    }
                }


            }
        }
    }

    /**
     * 获取随机一个方块的游标
     * @param farmlands
     * @return
     */
    private Integer getRandomBlock(List<Block> farmlands){

        int min = 0;
        int max = farmlands.size() - 1;

        Random rand = new Random();
        return rand.nextInt((max+1) - min) + min;
    }

    /**
     * 捡起地上的物品
     */
    @Override
    public void pickItems(){

        if(!isAlive()){
            return;
        }

        if(!getNpc().isSpawned()){
            return;
        }

        if(getFarmerInventory().size() >= 45){
            return;
        }

        List<Entity> nearbyEntities = getNpc().getEntity().getNearbyEntities(10, 2, 10);

        if(!nearbyEntities.isEmpty()){
            for(Entity entity : nearbyEntities){
                if(entity != null){
                    if (entity instanceof Item) {
                        Item i = (Item) entity;

                        if(i.getItemStack().getType().equals(Material.WHEAT_SEEDS)){
                            i.remove();
                            continue;
                        }

                        Set<String> picks = getCoiNpc().getPickItemMaterials();
                        if(picks != null && picks.size() > 0){

                            for(String pickItemName : picks){
                                Material material = Material.getMaterial(pickItemName);
                                if(material != null){
                                    if(i.getItemStack().getType() == material) {

                                        if(i.getLocation().distance(getNpc().getEntity().getLocation()) < 3){

                                            addItemToInventory(i.getItemStack());
                                            i.remove();
                                        }else{
                                            findPath(i.getLocation());
                                        }
                                    }
                                }

                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * 将物品添加到自己的专属背包
     * @param item
     */
    private void addItemToInventory(ItemStack item){
        if(item != null){
            List<ItemStack> backpack = getFarmerInventory();
            if(backpack.size() >= 45){

                say("我的背包满了，这些东西装不下了");
                getNpc().getEntity().getWorld().dropItem(getNpc().getEntity().getLocation(),item);
            }else{
                getFarmerInventory().add(item);
            }

        }
    }

    @Override
    public void move(){
        super.move();

        // 用小麦制作面包
        makeWheatToBread();

        // 寻找泥土并把泥土变成耕地
        findDirtAndMakeItToFarmland();

        // 寻找耕地并种植、收割小麦
        plantAndHarvestWheat();
    }

    public List<ItemStack> getFarmerInventory() {
        return farmerInventory;
    }

    public List<Block> getFarmlands() {
        return farmlands;
    }
}
