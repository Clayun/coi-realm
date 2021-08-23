package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COIBlock;
import com.mcylm.coi.realm.tools.npc.AI;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.goals.TargetNearbyEntityGoal;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

/**
 * 所有AI的父类
 * 通用AI，拥有基本生存能力，能捡东西，吃东西
 */
public class COIHuman implements AI {

    // CitizensNPC 实例
    private NPC npc;
    // COIAI 实例
    private COINpc coiNpc;
    // NPC饱食度
    private double hunger;
    // NPC是否已生成
    private boolean isSpawn = false;
    // Citizens 实例是否已创建
    private boolean isCreated = false;
    // 是否已经非常饥饿，无法工作
    private boolean isHungry = false;
    // 默认饥饿度
    private static final int DEFAULT_HUNGER = 20;
    // 饥饿度计算值间隔
    private static final int HUNGER_DELAY = 20;
    // 饥饿度计数值
    private int HUNGER_DELAY_COUNT = 0;
    // 最大生命值
    private final int MAX_HEALTH = 20;
    // 重生计数值
    private int RESPAWN_DELAY_COUNT = 0;
    // 是否正在重生COUNT
    private boolean isRespawning = false;

    // 构造NPC
    public COIHuman(COINpc npcCreator) {
        create(npcCreator);
    }

    /**
     * 创建 AI NPC 实例，万物之始
     * @param npcCreator
     * @return
     */
    @Override
    public COIHuman create(COINpc npcCreator) {

        if(npcCreator == null){
            LoggerUtils.log("npc创建对象为空，无法生成NPC");
            return null;
        }

        this.coiNpc = npcCreator;
        // 创建 CitizensNPC 实例
        this.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, getName());
        this.isCreated = true;

        initNpcAttributes(npcCreator);

        return this;
    }

    /**
     * 更新 NPC属性
     * @param coiNpc
     * @param respawn 是否重新生成，重新生成会清空背包
     * @return
     */
    @Override
    public COIHuman update(COINpc coiNpc,boolean respawn) {

        if(this.coiNpc == null || !isCreated){
            LoggerUtils.log("npc从未创建，无法更新");
            return null;
        }

        // 临时缓存老的NPC数据
        COINpc oldNpc = this.coiNpc;

        // 给NPC赋值新的数据
        this.coiNpc = coiNpc;

        if(respawn){
            // 清空背包
            this.coiNpc.setInventory(new ArrayList<>());
            // 清空食物袋
            this.coiNpc.setFoodBag(new ArrayList<>());

            // 如果NPC还存活
            if(isAlive()){
                // NPC的当前位置
                Location entityLocation = this.npc.getEntity().getLocation();
                // NPC重新生成
                despawn();
                spawn(entityLocation);
            }else{
                // 重生
                despawn();
                setHunger(DEFAULT_HUNGER);
                spawn(coiNpc.getSpawnLocation());
            }

        }else{
            // 还原背包
            this.coiNpc.setInventory(oldNpc.getInventory());
            this.coiNpc.setFoodBag(oldNpc.getFoodBag());
        }


        initNpcAttributes(this.coiNpc);

        return this;
    }

    /**
     * 自动寻路，默认的算法
     * @param location
     */
    @Override
    public void findPath(Location location) {
        if(npc == null){
            return;
        }

        if(!npc.isSpawned()){
            return;
        }

        npc.faceLocation(location);
        npc.getNavigator().setTarget(location);
    }

    /**
     * 控制 NPC 说话
     * @param message
     */
    @Override
    public void say(String message) {

        if(!isAlive()){
            return;
        }

        LoggerUtils.broadcastMessage(getName()+"："+message);
    }

    /**
     * 吃背包里的食物，恢复饥饿度，从而让血量恢复
     */
    @Override
    public void eatFood() {

        if(!isAlive()){
            return;
        }

        LivingEntity entity = (LivingEntity) npc.getEntity();

        if(getHunger() >= 15){
            // 如果饱食度大于等于15，就不再是饥饿状态
            isHungry = false;
        }

        if(getHunger() >= 20){
            return;
        }

        List<ItemStack> backpack = getCoiNpc().getFoodBag();
        // 在背包里找吃的
        if(!backpack.isEmpty()){
            Iterator<ItemStack> iterator = backpack.iterator();
            while (iterator.hasNext()) {
                ItemStack item = iterator.next();
                if(item != null){
                    List<String> npcFoods = Entry.getNpcFoods();
                    for(String foodName : npcFoods){
                        Material material = Material.getMaterial(foodName);

                        if(material != null){
                            if(item.getType() == material){
                                if(item.getAmount() >= 1){
                                    item.setAmount(item.getAmount() - 1);
                                    ItemStack cache = null;
                                    if(entity.getEquipment().getItemInMainHand() != null){
                                        cache = entity.getEquipment().getItemInMainHand();
                                    }
                                    entity.getEquipment().setItemInMainHand(item);
                                    setHunger(getHunger() + 1);
                                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EAT,100,2);
                                    entity.getEquipment().setItemInMainHand(cache);
                                    return;
                                }else{
                                    iterator.remove();
                                }
                            }
                        }
                    }


                }

            }
        }else{
            // 如果食物背包里是空的，同时饱食度低于10，就去磨坊拿吃的

            if(getHunger() <= 10){
                isHungry = true;
            }


        }

    }

    /**
     * 去磨坊寻找食物
     */
    private void findFood(){

        if(!isTooHungryToWork()){
            return;
        }

        List<Location> foodChests = getCoiNpc().getTeam().getFoodChests();

        if(foodChests == null
                || foodChests.isEmpty()){
            LoggerUtils.debug("食物箱子不存在");
            return;
        }

        Location nearestLocation = null;
        double distance = 99999d;

        for(Location location : foodChests){

            Material material = location.getBlock().getType();

            if(material.equals(Material.CHEST)){

                Chest block = (Chest) location.getBlock().getState();

                // 不为空
                if(block.getInventory().isEmpty()){
                    continue;
                }
                double locationDistance = location.distance(getNpc().getEntity().getLocation());

                // 冒泡排序
                if(locationDistance <= distance){
                    nearestLocation = location;
                    distance = locationDistance;
                }

            }
        }

        // 最近的食物箱子
        if(nearestLocation != null){

            LoggerUtils.debug("最近的一个有食物的箱子位置在："+nearestLocation.getX()+","+nearestLocation.getZ());

            // 如果距离大于3，就走过去
            if(distance > 3){
                findPath(nearestLocation);
            }

            // 距离小于等于3，拿物品
            if(distance <= 3){
                // 一次性拿20个

                // 扣减箱子里的物品，扣20个，不足20个有多少拿多少
                int i = ItemUtils.takeItemFromChest(nearestLocation, Material.BREAD, 20);

                if(i > 0){

                    // 将扣减的数量添加到背包里
                    for(int count = 0; count < i; count++){
                        addItemToFoodBag(new ItemStack(Material.BREAD));
                    }

                }
            }
        }

    }

    /**
     * 获取箱子当中的全部物品
     * @param location
     * @return
     */
    private List<ItemStack> getChest(Location location){

        List<ItemStack> results = new ArrayList<>();

        Block block = location.getBlock();

        if(block.getType().equals(Material.CHEST)){
            Chest chest = (Chest) block.getState();
            Inventory blockInventory = chest.getBlockInventory();

            @NonNull ItemStack[] contents = blockInventory.getContents();

            results = new ArrayList<>(Arrays.asList(contents));
        }

        return results;
    }

    /**
     * 吃饱了就回血
     */
    private void fullStomach(){

        if(!isAlive()){
            return;
        }

        LivingEntity entity = (LivingEntity) npc.getEntity();

        if(getHunger() >= 20){
            //自动回血
            if(entity.getHealth() < MAX_HEALTH){

                if(MAX_HEALTH - entity.getHealth() >= 1){
                    entity.setHealth(entity.getHealth() + 1);
                }else{
                    entity.setHealth(MAX_HEALTH);
                }
            }
        }

        //减少饱食度
        if(HUNGER_DELAY_COUNT < HUNGER_DELAY){
            if(npc.getNavigator().isNavigating()){
                HUNGER_DELAY_COUNT = HUNGER_DELAY_COUNT + 2;
            }else{
                HUNGER_DELAY_COUNT ++;
            }

        }else{
            if(getHunger() > 0){
                setHunger(getHunger() - 1);
                HUNGER_DELAY_COUNT = 0;
            }
        }

        if(getHunger() <= 0){
            if(entity.getHealth() > 1){
                entity.setHealth(entity.getHealth() - 1);
            }
        }

        updateName();
    }

    /**
     * 穿衣服
     * todo 需要优化选择更好的物品装备
     */
    @Override
    public void wearClothes() {

        if(!isAlive()){
            return;
        }

        List<ItemStack> backpack = getCoiNpc().getInventory();
        LivingEntity entity = (LivingEntity) npc.getEntity();

        Iterator<ItemStack> iterator = backpack.iterator();

        while(iterator.hasNext()){
            ItemStack itemStack = iterator.next();
            //头盔
            if(itemStack.getType() == Material.CHAINMAIL_HELMET
                    || itemStack.getType() == Material.DIAMOND_HELMET
                    || itemStack.getType() == Material.GOLDEN_HELMET
                    || itemStack.getType() == Material.IRON_HELMET
                    || itemStack.getType() == Material.LEATHER_HELMET
                    || itemStack.getType() == Material.NETHERITE_HELMET
                    || itemStack.getType() == Material.TURTLE_HELMET
            ){
                if(itemStack.getType().equals(Material.LEATHER_HELMET)){
                    // 皮革的，就换成小队颜色
                    if(getCoiNpc().getTeam().getType().getLeatherColor() != null){
                        ItemUtils.changeColorForLeather(itemStack,getCoiNpc().getTeam().getType().getLeatherColor());
                    }
                }
                entity.getEquipment().setHelmet(itemStack);
                entity.getEquipment().setHelmet(itemStack);
                iterator.remove();
                LoggerUtils.debug("NPC穿上了头盔");
                continue;
            }

            //胸甲
            if(itemStack.getType() == Material.CHAINMAIL_CHESTPLATE
                    || itemStack.getType() == Material.DIAMOND_CHESTPLATE
                    || itemStack.getType() == Material.GOLDEN_CHESTPLATE
                    || itemStack.getType() == Material.IRON_CHESTPLATE
                    || itemStack.getType() == Material.LEATHER_CHESTPLATE
                    || itemStack.getType() == Material.NETHERITE_CHESTPLATE
            ){

                if(itemStack.getType().equals(Material.LEATHER_CHESTPLATE)){
                    // 皮革的，就换成小队颜色
                    if(getCoiNpc().getTeam().getType().getLeatherColor() != null){
                        ItemUtils.changeColorForLeather(itemStack,getCoiNpc().getTeam().getType().getLeatherColor());
                    }
                }
                entity.getEquipment().setChestplate(itemStack);
                entity.getEquipment().setChestplate(itemStack);
                iterator.remove();
                LoggerUtils.debug("NPC穿上了胸甲");
                continue;
            }

            //裤子
            if(itemStack.getType() == Material.LEATHER_LEGGINGS
                    || itemStack.getType() == Material.CHAINMAIL_LEGGINGS
                    || itemStack.getType() == Material.DIAMOND_LEGGINGS
                    || itemStack.getType() == Material.GOLDEN_LEGGINGS
                    || itemStack.getType() == Material.IRON_LEGGINGS
                    || itemStack.getType() == Material.NETHERITE_LEGGINGS
            ){

                if(itemStack.getType().equals(Material.LEATHER_LEGGINGS)){
                    // 皮革的，就换成小队颜色
                    if(getCoiNpc().getTeam().getType().getLeatherColor() != null){
                        ItemUtils.changeColorForLeather(itemStack,getCoiNpc().getTeam().getType().getLeatherColor());
                    }
                }
                entity.getEquipment().setLeggings(itemStack);
                entity.getEquipment().setLeggings(itemStack);
                iterator.remove();
                LoggerUtils.debug("NPC穿上了裤子");
                continue;
            }

            //靴子
            if(itemStack.getType() == Material.CHAINMAIL_BOOTS
                    || itemStack.getType() == Material.DIAMOND_BOOTS
                    || itemStack.getType() == Material.GOLDEN_BOOTS
                    || itemStack.getType() == Material.IRON_BOOTS
                    || itemStack.getType() == Material.LEATHER_BOOTS
                    || itemStack.getType() == Material.NETHERITE_BOOTS
            ){

                if(itemStack.getType().equals(Material.LEATHER_BOOTS)){
                    // 皮革的，就换成小队颜色
                    if(getCoiNpc().getTeam().getType().getLeatherColor() != null){
                        ItemUtils.changeColorForLeather(itemStack,getCoiNpc().getTeam().getType().getLeatherColor());
                    }
                }

                entity.getEquipment().setBoots(itemStack);
                entity.getEquipment().setBoots(itemStack);
                iterator.remove();
                LoggerUtils.debug("NPC穿上了鞋");
                continue;
            }

            //武器或工具
            //剑
            if(itemStack.getType() == Material.DIAMOND_SWORD
                    || itemStack.getType() == Material.STONE_SWORD
                    || itemStack.getType() == Material.WOODEN_SWORD
                    || itemStack.getType() == Material.GOLDEN_SWORD
                    || itemStack.getType() == Material.IRON_SWORD
                    || itemStack.getType() == Material.NETHERITE_SWORD
                    //斧头
                    || itemStack.getType() == Material.DIAMOND_AXE
                    || itemStack.getType() == Material.GOLDEN_AXE
                    || itemStack.getType() == Material.IRON_AXE
                    || itemStack.getType() == Material.NETHERITE_AXE
                    || itemStack.getType() == Material.STONE_AXE
                    || itemStack.getType() == Material.WOODEN_AXE
                    //镐子
                    || itemStack.getType() == Material.DIAMOND_PICKAXE
                    || itemStack.getType() == Material.GOLDEN_PICKAXE
                    || itemStack.getType() == Material.IRON_PICKAXE
                    || itemStack.getType() == Material.NETHERITE_PICKAXE
                    || itemStack.getType() == Material.STONE_PICKAXE
                    || itemStack.getType() == Material.WOODEN_PICKAXE
                    //锄头
                    || itemStack.getType() == Material.DIAMOND_HOE
                    || itemStack.getType() == Material.GOLDEN_HOE
                    || itemStack.getType() == Material.IRON_HOE
                    || itemStack.getType() == Material.NETHERITE_HOE
                    || itemStack.getType() == Material.STONE_HOE
                    || itemStack.getType() == Material.WOODEN_HOE
            ){
                if(entity.getEquipment().getItemInMainHand().getType().equals(Material.AIR)){
                    entity.getEquipment().setItemInMainHand(itemStack);
                    iterator.remove();
                    LoggerUtils.debug("NPC装备了武器或工具");
                    continue;
                }
            }
        }


    }

    /**
     * 捡起地上的物品
     */
    @Override
    public void pickItems(){

        if(!isAlive()){
            return;
        }

        if(!npc.isSpawned()){
            return;
        }

        if(getCoiNpc().getInventory().size() >= 45){
            return;
        }

        List<Entity> nearbyEntities = npc.getEntity().getNearbyEntities(10, 2, 10);

        if(!nearbyEntities.isEmpty()){
            for(Entity entity : nearbyEntities){
                if(entity != null){
                    if (entity instanceof Item) {
                        Item i = (Item) entity;

                        Set<String> picks = getCoiNpc().getPickItemMaterials();
                        if(picks != null && picks.size() > 0){

                            for(String pickItemName : picks){
                                Material material = Material.getMaterial(pickItemName);
                                if(material != null){
                                    if(i.getItemStack().getType() == material) {

                                        if(i.getLocation().distance(npc.getEntity().getLocation()) < 3){
                                            List<String> npcFoods = Entry.getNpcFoods();

                                            if(npcFoods == null || npcFoods.isEmpty()){
                                                // 普通物品
                                                addItemToInventory(i.getItemStack());
                                                i.remove();
                                            }else{

                                                // 循环匹配食物
                                                boolean isMatch = false;
                                                for(String foodName : npcFoods){
                                                    Material food = Material.getMaterial(foodName);
                                                    if(food.equals(i.getItemStack().getType())){
                                                        addItemToFoodBag(i.getItemStack());
                                                        i.remove();
                                                        isMatch = true;
                                                        break;
                                                    }
                                                }

                                                // 未匹配到，就当普通物品存进去
                                                if(!isMatch){
                                                    addItemToInventory(i.getItemStack());
                                                    i.remove();
                                                }
                                            }


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
     * 将物品添加到背包里
     * @param item
     */
    private void addItemToInventory(ItemStack item){
        if(item != null){
            List<ItemStack> backpack = getCoiNpc().getInventory();
            if(backpack.size() >= 45){

                say("我的背包满了，这些东西装不下了");
                npc.getEntity().getWorld().dropItem(npc.getEntity().getLocation(),item);
            }else{
                getCoiNpc().getInventory().add(item);
            }

        }
    }

    /**
     * 将物品添加到食品袋里
     * @param item
     */
    private void addItemToFoodBag(ItemStack item){
        if(item != null){
            List<ItemStack> backpack = getCoiNpc().getFoodBag();
            if(backpack.size() >= 45){

                say("我的背包满了，这些东西装不下了");
                npc.getEntity().getWorld().dropItem(npc.getEntity().getLocation(),item);
            }else{
                getCoiNpc().getFoodBag().add(item);
            }

        }
    }

    @Override
    public void move() {
        // 优先穿衣服
        wearClothes();
        // 捡起附近需要的物品 使用同步进程去做
        pickItems();
        // 吃饱了回血
        fullStomach();
        // 没吃饱就去吃
        eatFood();
        // 寻找食物
        findFood();
        // 检查死没死
        dead();
    }

    /**
     * 生成NPC
     */
    @Override
    public void spawn(Location location) {

        if(npc.isSpawned() && this.isSpawn){
            LoggerUtils.log("NPC已spawn，无法再次生成");
            return;
        }

        // 更新NPC的名称
        updateName();

        npc.spawn(location);

        // 恢复血量和饱食度
        initEntityStatus();

        npc.setProtected(false);
        this.isSpawn = true;
    }

    @Override
    public void despawn() {
        if(this.npc != null){
            npc.despawn();
            this.isSpawn = false;
        }
    }

    /**
     * NPC是否还存活
     * @return
     */
    @Override
    public boolean isAlive() {

        if(npc.getEntity() == null || npc.getEntity().isDead()){
            return false;
        }

        return true;
    }

    @Override
    public Location getLocation() {

        if(isAlive()){
            return npc.getEntity().getLocation();
        }
        return null;
    }

    /**
     * NPC死亡时调用本方法
     */
    private void dead() {

        // 判断是否还活着
        if(isAlive()){
            return;
        }

        if(coiNpc.isCanRespawn()){
            if(!isRespawning){
                isRespawning = true;
                LoggerUtils.debug("NPC死亡了");
                // 死亡掉落全部物资
                dropAllItems();
            }

            if(isRespawning){
                RESPAWN_DELAY_COUNT++;
                if(RESPAWN_DELAY_COUNT == coiNpc.getRespawnDelay()){
                    RESPAWN_DELAY_COUNT = 0;
                    isRespawning = false;

                    // 复活
                    spawn(coiNpc.getSpawnLocation());
                    LoggerUtils.debug("NPC复活了");
                }
            }
        }

    }

    /**
     * NPC死亡触发物品全部掉落
     */
    public void dropAllItems(){
        List<ItemStack> foodBag = getCoiNpc().getFoodBag();
        Iterator<ItemStack> foods = foodBag.iterator();

        while(foods.hasNext()){
            ItemStack food = foods.next();
            npc.getEntity().getWorld().dropItem(npc.getEntity().getLocation(),food);
        }

        List<ItemStack> inventory = getCoiNpc().getInventory();
        Iterator<ItemStack> items = inventory.iterator();

        while (items.hasNext()){
            ItemStack next = items.next();
            npc.getEntity().getWorld().dropItem(npc.getEntity().getLocation(),next);
        }
    }

    /**
     * 内部方法，获取NPC名称
     * @return
     */
    private String getName(){

        String teamColor = "";

        // 名字改为小队颜色
        if(getCoiNpc().getTeam() != null){
            teamColor = getCoiNpc().getTeam().getType().getColor();
        }

        String hungerColor = "&a";

        if(getHunger() >= 10){
            hungerColor = "&a";
        }else if(getHunger() >= 5){
            hungerColor = "&6";
        }else if(getHunger() < 5){
            hungerColor = "&c";
        }

        // 名字组成 Lv.1 矿工 20.0
        return LoggerUtils.replaceColor(teamColor + "Lv."+coiNpc.getLevel()+" "+coiNpc.getName() + " "+hungerColor + getHunger());
    }

    /**
     * 内部方法，更新NPC 名称
     */
    private void updateName(){

        // 名字变了再更新
        if(!this.npc.getName().equals(getName())){
            this.npc.setName(getName());
        }

    }

    /**
     * 初始化NPC的参数
     */
    private void initNpcAttributes(COINpc npcCreator){

        // 恢复血量和饱食度
        initEntityStatus();

        // 如果背包未初始化
        if(npcCreator.getInventory() == null){
            npcCreator.setInventory(new ArrayList<>());
        }

        if(npcCreator.getFoodBag() == null){
            npcCreator.setFoodBag(new ArrayList<>());
        }

        // 设置NPC的名称使用 Hologram
        this.npc.setAlwaysUseNameHologram(true);

        // 初始化NPC的皮肤
        this.npc.data().set("player-skin-textures",npcCreator.getSkinTextures());
        this.npc.data().set("cached-skin-uuid-name",npcCreator.getSkinName());
        this.npc.data().set("player-skin-name",npcCreator.getSkinName());
        this.npc.data().set("player-skin-signature",npcCreator.getSkinSignature());
        this.npc.data().set("player-skin-use-latest-skin",false);

        // 设置NPC的敌对生物
        // 如果设置了主动攻击，则NPC会主动攻击敌对生物
        Set<EntityType> targets = npcCreator.getEnemyEntities();
        if(targets != null){
            TargetNearbyEntityGoal targetNearbyEntityGoal = TargetNearbyEntityGoal.builder(npc)
                    .targets(targets).aggressive(npcCreator.isAggressive()).radius(npcCreator.getAlertRadius()).build();
            this.npc.getDefaultGoalController().addGoal(targetNearbyEntityGoal,1);
        }
    }

    /**
     * 初始化生物状态
     */
    private void initEntityStatus(){
        // 饥饿值默认20
        setHunger(DEFAULT_HUNGER);

        if(isAlive()){
            // 满血
            LivingEntity entity = (LivingEntity) npc.getEntity();

            entity.setHealth(MAX_HEALTH);
        }
    }

    /**
     * 获取 NPC 范围内的方块
     * @param radius
     * @return
     */
    @Override
    public List<Block> getNearbyBlocks(double radius) {
        if(getNpc() == null){
            return new ArrayList<>();
        }

        List<Block> list = new ArrayList<>();

        Location nowLoc = getNpc().getEntity().getLocation();
        double minX = nowLoc.getX() - radius;
        double maxX = nowLoc.getX() + radius;
        double minY = nowLoc.getY()-1;
        double maxY = nowLoc.getY()+3;
        double minZ = nowLoc.getZ() - radius;
        double maxZ = nowLoc.getZ() + radius;

        for(double x = minX;x < maxX; x ++){
            for(double y = minY;y < maxY; y ++){
                for(double z = minZ;z < maxZ; z ++){
                    Block blockAt = getNpc().getEntity().getWorld().getBlockAt(new Location(getNpc().getEntity().getWorld(), x, y, z));
                    if(blockAt.getType() != Material.AIR){
                        list.add(blockAt);
                    }
                }
            }
        }

        //根据Y轴排序
        Collections.sort(list, Comparator.comparingDouble(Block::getY));
        //再次翻转，从高到低
        Collections.reverse(list);

        return list;
    }

    @Override
    public List<Entity> getNearByEntities(double radius) {

        if(!isAlive()){
            return new ArrayList<>();
        }

        List<Entity> result = new ArrayList<>();

        List<Entity> nearbyEntities = npc.getEntity().getNearbyEntities(radius, 2, radius);

        if(!nearbyEntities.isEmpty()){
            for(Entity entity : nearbyEntities){
                if(entity != null){
                    if (entity.getType().equals(EntityType.PLAYER)) {
                        // 实体是玩家
                        result.add(entity);
                    }else if(entity instanceof LivingEntity){
                        // 普通生物
                        result.add(entity);
                    }
                }

            }
        }

        return result;
    }

    public NPC getNpc() {
        return npc;
    }

    public COINpc getCoiNpc() {
        return coiNpc;
    }

    public boolean isSpawn() {
        return isSpawn;
    }

    public boolean isCreated() {
        return isCreated;
    }

    public double getHunger() {
        return hunger;
    }

    /**
     * 内部方法，设置NPC的饥饿度
     * @param hunger
     */
    private void setHunger(double hunger) {
        this.hunger = hunger;
    }

    /**
     * 获取NPC最大血量
     * @return
     */
    private double getMaxHealth(){
        return MAX_HEALTH;
    }

    /**
     * 饿的不能继续工作了
     * @return
     */
    public boolean isTooHungryToWork() {

        if(isHungry){
            // 食物背包里的物品为空
            if(getCoiNpc().getFoodBag().isEmpty()){
                return true;
            }
        }

        return false;
    }
}
